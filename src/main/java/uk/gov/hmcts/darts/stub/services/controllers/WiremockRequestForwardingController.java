package uk.gov.hmcts.darts.stub.services.controllers;

import io.micrometer.core.instrument.util.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.LinkedMultiValueMap;
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
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;

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

    private final HttpClient httpClient;

    private final MockHttpServer mockHttpServer;

    public WiremockRequestForwardingController(HttpClient httpClient, MockHttpServer mockHttpServer) {
        this.httpClient = httpClient;
        this.mockHttpServer = mockHttpServer;
    }

    @GetMapping("**")
    public ResponseEntity<Object> forwardGetRequests(HttpServletRequest request) throws InterruptedException {
        try {
            var requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            var requestBuilder = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath)));
            var forwardRequest = copyHeaders(request, requestBuilder).GET().build();

            var httpResponse = httpClient.send(forwardRequest, HttpResponse.BodyHandlers.ofString());
            return getObjectResponseEntity(httpResponse);
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }



    @PostMapping("**")
    public ResponseEntity<Object> forwardPostRequests(HttpServletRequest request) throws InterruptedException {
        try {
            var requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            var requestBody = IOUtils.toString(request.getInputStream());
            var requestBuilder = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath)));
            var forwardRequest =
                copyHeaders(request, requestBuilder)
                    .POST(BodyPublishers.ofString(requestBody))
                    .build();

            var httpResponse = httpClient.send(forwardRequest, HttpResponse.BodyHandlers.ofString());
            return getObjectResponseEntity(httpResponse);
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    @PutMapping("**")
    public ResponseEntity<Object> forwardPutRequests(HttpServletRequest request) throws InterruptedException {
        try {
            var requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            var requestBody = IOUtils.toString(request.getInputStream());
            var requestBuilder = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath)));
            var forwardRequest = copyHeaders(request, requestBuilder).PUT(BodyPublishers.ofString(requestBody)).build();

            var httpResponse = httpClient.send(forwardRequest, HttpResponse.BodyHandlers.ofString());
            return getObjectResponseEntity(httpResponse);
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    @DeleteMapping("**")
    public ResponseEntity<Object> forwardDeleteRequests(HttpServletRequest request) throws InterruptedException {
        try {
            var requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            var requestBuilder = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath)));
            var forwardRequest = copyHeaders(request, requestBuilder).DELETE().build();

            var httpResponse = httpClient.send(forwardRequest, HttpResponse.BodyHandlers.ofString());

            return new ResponseEntity<>(
                httpResponse.body(),
                valueOf(httpResponse.statusCode())
            );
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    private ResponseEntity<Object> getObjectResponseEntity(HttpResponse<String> httpResponse) {
        var responseContentType = httpResponse.headers().firstValue("Content-Type").orElse(null);
        var headers = new LinkedMultiValueMap<String, String>();
        if (responseContentType != null) {
            headers.add("Content-Type", responseContentType);
        }

        return new ResponseEntity<>(
            httpResponse.body(),
            headers,
            valueOf(httpResponse.statusCode())
        );
    }

    private static Builder copyHeaders(HttpServletRequest request, Builder requestBuilder) {
        var headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            var headerName = headerNames.nextElement();
            if (notExcluded(headerName)) {
                requestBuilder.header(headerName, request.getHeader(headerName));
            }
        }

        return requestBuilder;
    }

    private static boolean notExcluded(String headerName) {
        return !"host".equalsIgnoreCase(headerName)
            && !"connection".equalsIgnoreCase(headerName)
            && !"content-length".equalsIgnoreCase(headerName);
    }

    private String getMockHttpServerUrl(String requestPath) {
        return "http://" + mockHttpServerHost + ":" + mockHttpServer.portNumber() + requestPath;
    }
}
