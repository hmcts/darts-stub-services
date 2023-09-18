package uk.gov.hmcts.darts.stub.services.controllers;

import io.micrometer.core.instrument.util.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.darts.stub.services.server.MockHttpServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.valueOf;

@RestController
@RequestMapping("/")
@SuppressWarnings("PMD.LawOfDemeter")
public class WiremockRequestForwardingController {

    private static final Logger LOG = LoggerFactory.getLogger(WiremockRequestForwardingController.class);
    public static final String ERROR_OCCURRED = "Error occurred";

    @Value("${wiremock.server.host}")
    private String mockHttpServerHost;

    private final RestTemplate restTemplate;

    private final MockHttpServer mockHttpServer;
    private final HttpClient httpClient;

    public WiremockRequestForwardingController(RestTemplate restTemplate, MockHttpServer mockHttpServer,
                                               HttpClient httpClient) {
        this.restTemplate = restTemplate;
        this.mockHttpServer = mockHttpServer;
        this.httpClient = httpClient;
    }

    @GetMapping("**")
    public ResponseEntity<String> forwardGetRequests(HttpServletRequest request) {
        return forwardRequestForMethod(request, GET);
    }


    @PostMapping("**")
    public ResponseEntity<String> forwardPostRequests(HttpServletRequest request)
        throws InterruptedException, IOException {
        try {
            var requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            final var requestBody = IOUtils.toString(request.getInputStream());
            var postBuilder = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath)))
                .POST(BodyPublishers.ofString(requestBody));
            addHeaders(request, postBuilder);

            var httpResponse = httpClient.send(postBuilder.build(), HttpResponse.BodyHandlers.ofString());
            var responseHeaders = copyResponseHeaders(httpResponse.headers());
            return new ResponseEntity<>(
                httpResponse.body(),
                responseHeaders,
                valueOf(httpResponse.statusCode())
            );
        } catch (IOException | InterruptedException e) {
            LOG.error("Error occurred", e);
            throw e;
        }
    }

    private MultiValueMap<String, String> copyResponseHeaders(HttpHeaders headers) {
        var headerMap = new LinkedMultiValueMap<String, String>();
        headers.map().forEach((key, value) -> headerMap.add(key, join(";", value)));
        return headerMap;
    }

    private void addHeaders(HttpServletRequest request, Builder postBuilder) {
        copyHeaders(request).forEach((key, value) -> postBuilder.header(key, join(";", value)));
    }

    @PutMapping("**")
    public ResponseEntity<String> forwardPutRequests(HttpServletRequest request) {
        return forwardRequestForMethod(request, PUT);
    }

    @DeleteMapping("**")
    public ResponseEntity<String> forwardDeleteRequests(HttpServletRequest request) {
        return forwardRequestForMethod(request, DELETE);
    }

    private ResponseEntity<String> forwardRequestForMethod(HttpServletRequest request, HttpMethod get) {
        try {
            var requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            var uri = URI.create(getMockHttpServerUrl(requestPath));
            var requestBody = IOUtils.toString(request.getInputStream(), UTF_8);
            var headers = copyHeaders(request);
            var forwardRequest = new RequestEntity<>(requestBody, headers, get, uri);

            return restTemplate.exchange(forwardRequest, String.class);
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    private static LinkedMultiValueMap<String, String> copyHeaders(HttpServletRequest request) {
        var headerNames = request.getHeaderNames();
        var headers = new LinkedMultiValueMap<String, String>();
        while (headerNames.hasMoreElements()) {
            var headerName = headerNames.nextElement();
            if (!excluded(headerName)) {
                headers.add(headerName, request.getHeader(headerName));
            }
        }

        return headers;
    }

    private static boolean excluded(String headerName) {
        return "host".equalsIgnoreCase(headerName)
            || "connection".equalsIgnoreCase(headerName)
            || "content-length".equalsIgnoreCase(headerName);
    }

    private String getMockHttpServerUrl(String requestPath) {
        return "http://" + mockHttpServerHost + ":" + mockHttpServer.portNumber() + requestPath;
    }
}
