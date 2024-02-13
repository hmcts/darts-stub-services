package uk.gov.hmcts.darts.stub.services.controllers;

import io.micrometer.core.instrument.util.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.darts.stub.services.server.MockHttpServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Set;

import static java.net.http.HttpRequest.BodyPublisher;
import static java.net.http.HttpRequest.BodyPublishers;
import static java.net.http.HttpRequest.Builder;
import static java.net.http.HttpRequest.newBuilder;
import static java.util.Locale.ENGLISH;

@RestController
@RequestMapping("/")
@SuppressWarnings("PMD.LawOfDemeter")
public class WiremockRequestForwardingController {

    private static final Logger LOG = LoggerFactory.getLogger(WiremockRequestForwardingController.class);
    private static final String CATCH_ALL_PATH = "**";
    private static final Set<String> EXCLUDED_HEADERS = Set.of();

    @Value("${wiremock.server.host}")
    private String mockHttpServerHost;

    private final HttpClient httpClient;
    private final MockHttpServer mockHttpServer;

    public WiremockRequestForwardingController(HttpClient httpClient, MockHttpServer mockHttpServer) {
        this.httpClient = httpClient;
        this.mockHttpServer = mockHttpServer;
    }

    @GetMapping(CATCH_ALL_PATH)
    public ResponseEntity<Resource> forwardGetRequests(HttpServletRequest request)
            throws IOException, InterruptedException {
        LOG.info("getting a get request");
        var requestBody = IOUtils.toString(request.getInputStream());
        return forwardRequest(request, BodyPublishers.ofString(requestBody), HttpMethod.GET);
    }

    @PostMapping(CATCH_ALL_PATH)
    public ResponseEntity<Resource> forwardPostRequests(HttpServletRequest request)
            throws IOException, InterruptedException {
        LOG.info("getting a post request");
        var requestBody = IOUtils.toString(request.getInputStream());
        return forwardRequest(request, BodyPublishers.ofString(requestBody), HttpMethod.POST);
    }

    @PutMapping(CATCH_ALL_PATH)
    public ResponseEntity<Resource> forwardPutRequests(HttpServletRequest request)
            throws IOException, InterruptedException {
        LOG.info("getting a put request");
        var requestBody = IOUtils.toString(request.getInputStream());
        return forwardRequest(request, BodyPublishers.ofString(requestBody), HttpMethod.PUT);
    }

    @DeleteMapping(CATCH_ALL_PATH)
    public ResponseEntity<Resource> forwardDeleteRequests(HttpServletRequest request)
            throws IOException, InterruptedException {
        LOG.info("getting a delete request");
        return forwardRequest(request, BodyPublishers.noBody(), HttpMethod.DELETE);
    }

    private ResponseEntity<Resource> forwardRequest(
            HttpServletRequest request,
            BodyPublisher bodyPublisher,
            HttpMethod httpMethod) throws IOException, InterruptedException {
        LOG.info("Got a request");
        var requestPath = new AntPathMatcher().extractPathWithinPattern(CATCH_ALL_PATH, request.getRequestURI());
        LOG.info("requestPath=" + requestPath);
        var requestBuilder = newBuilder(URI.create(getMockHttpServerUrl(requestPath)))
                .method(httpMethod.name(), bodyPublisher);

        LOG.info("about to transfer requestheaders");
        transferRequestHeaders(request, requestBuilder);

        var httpResponse = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
        LOG.info("got response httpResponse.statusCode() " + httpResponse.statusCode());
        return new ResponseEntity<>(
                new InputStreamResource(httpResponse.body()),
                copyResponseHeaders(httpResponse),
                httpResponse.statusCode()
        );
    }

    private void transferRequestHeaders(HttpServletRequest request, Builder requestBuilder) {
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            if (!EXCLUDED_HEADERS.contains(headerName.toLowerCase(ENGLISH))) {
                LOG.info("Adding header " + headerName + " header value " + request.getHeader(headerName));
                requestBuilder.header(headerName, request.getHeader(headerName));
            }
        });
    }

    private HttpHeaders copyResponseHeaders(HttpResponse<?> response) {
        HttpHeaders headers = new HttpHeaders();
        response.headers().map().forEach(headers::addAll);
        return headers;
    }

    private String getMockHttpServerUrl(String requestPath) {
        return "http://" + mockHttpServerHost + ":" + mockHttpServer.portNumber() + requestPath;
    }
}
