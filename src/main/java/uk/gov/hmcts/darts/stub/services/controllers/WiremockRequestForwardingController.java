package uk.gov.hmcts.darts.stub.services.controllers;

import io.micrometer.core.instrument.util.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.darts.stub.services.server.MockHttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

import static java.lang.String.join;
import static java.net.http.HttpRequest.BodyPublisher;
import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpRequest.Builder;
import static java.net.http.HttpRequest.newBuilder;
import static java.util.Locale.ENGLISH;

@RestController
@RequestMapping("/")
@SuppressWarnings("PMD.LawOfDemeter")
public class WiremockRequestForwardingController {

    private static final String CATCH_ALL_PATH = "**";
    private static final Set<String> EXCLUDED_REQUEST_HEADERS = Set.of(
            "host", "connection", "accept-encoding", "content-length", "transfer-encoding"
    );

    private static final Set<String> EXCLUDED_RESPONSE_HEADERS = Set.of(
            "transfer-encoding", ":status"
    );
    private final HttpClient httpClient;

    @Value("${wiremock.server.host}")
    private String mockHttpServerHost;

    private final MockHttpServer mockHttpServer;

    public WiremockRequestForwardingController(MockHttpServer mockHttpServer) {
        this.httpClient = HttpClient.newBuilder().version(Version.HTTP_1_1).build();
        this.mockHttpServer = mockHttpServer;
    }

    @GetMapping(CATCH_ALL_PATH)
    public ResponseEntity<Resource> forwardGetRequests(HttpServletRequest request)
            throws IOException, InterruptedException {
        String requestBody = IOUtils.toString(request.getInputStream());
        return forwardRequest(request, BodyPublishers.ofString(requestBody), HttpMethod.GET);
    }

    @PostMapping(CATCH_ALL_PATH)
    public ResponseEntity<Resource> forwardPostRequests(HttpServletRequest request)
            throws IOException, InterruptedException {
        String requestBody = IOUtils.toString(request.getInputStream());
        return forwardRequest(request, BodyPublishers.ofString(requestBody), HttpMethod.POST);
    }

    @PutMapping(CATCH_ALL_PATH)
    public ResponseEntity<Resource> forwardPutRequests(HttpServletRequest request)
            throws IOException, InterruptedException {
        String requestBody = IOUtils.toString(request.getInputStream());
        return forwardRequest(request, BodyPublishers.ofString(requestBody), HttpMethod.PUT);
    }

    @DeleteMapping(CATCH_ALL_PATH)
    public ResponseEntity<Resource> forwardDeleteRequests(HttpServletRequest request)
            throws IOException, InterruptedException {
        return forwardRequest(request, BodyPublishers.noBody(), HttpMethod.DELETE);
    }

    private ResponseEntity<Resource> forwardRequest(
            HttpServletRequest request,
            BodyPublisher bodyPublisher,
            HttpMethod httpMethod) throws IOException, InterruptedException {

        String requestPath = new AntPathMatcher().extractPathWithinPattern(CATCH_ALL_PATH, request.getRequestURI());
        HttpRequest.Builder requestBuilder = newBuilder(URI.create(getMockHttpServerUrl(requestPath)))
                .method(httpMethod.name(), bodyPublisher);

        transferRequestHeaders(request, requestBuilder);

        HttpResponse<InputStream> httpResponse = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());

        return new ResponseEntity<>(
                new InputStreamResource(httpResponse.body()),
                copyResponseHeaders(httpResponse),
                httpResponse.statusCode()
        );
    }

    private void transferRequestHeaders(HttpServletRequest request, Builder requestBuilder) {
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            if (!EXCLUDED_REQUEST_HEADERS.contains(headerName.toLowerCase(ENGLISH))) {
                String value = request.getHeader(headerName);
                requestBuilder.header(headerName, value);
            }
        });
    }

    private MultiValueMap<String, String>  copyResponseHeaders(HttpResponse<?> response) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        response.headers().map().forEach((key, values) -> {
            if (!EXCLUDED_RESPONSE_HEADERS.contains(key.toLowerCase(ENGLISH))) {
                headers.add(key, join(",", values));
            }
        });
        return headers;
    }

    private String getMockHttpServerUrl(String requestPath) {
        return "http://" + mockHttpServerHost + ":" + mockHttpServer.portNumber() + requestPath;
    }
}