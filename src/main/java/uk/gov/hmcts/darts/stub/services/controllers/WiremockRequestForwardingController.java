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
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NO_CONTENT;
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
    public ResponseEntity<byte[]> forwardGetRequests(HttpServletRequest request) throws InterruptedException {
        try {
            String requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            Builder requestBuilder = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath)));
            transferRequestHeaders(request, requestBuilder);

            var httpResponse = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            var responseHeaders = copyResponseHeaders(httpResponse);

            return new ResponseEntity<byte[]>(
                httpResponse.body().getBytes(),
                responseHeaders,
                valueOf(httpResponse.statusCode()));
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(e.getMessage().getBytes());
        }
    }


    @PostMapping("**")
    public ResponseEntity<Object> forwardPostRequests(HttpServletRequest request) throws InterruptedException {
        try {
            var requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            var requestBody = IOUtils.toString(request.getInputStream());
            var requestBuilder = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath)))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody));
            transferRequestHeaders(request, requestBuilder);

            var httpResponse = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            var responseHeaders = copyResponseHeaders(httpResponse);

            return new ResponseEntity<>(
                httpResponse.body().getBytes(),
                responseHeaders,
                valueOf(httpResponse.statusCode())
            );
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(e.getMessage().getBytes());
        }
    }

    @PutMapping("**")
    public ResponseEntity<byte[]> forwardPutRequests(HttpServletRequest request) throws InterruptedException {
        try {
            var requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            var requestBody = IOUtils.toString(request.getInputStream());
            var requestBuilder = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath)))
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody));
            transferRequestHeaders(request, requestBuilder);

            var httpResponse = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            var responseHeaders = copyResponseHeaders(httpResponse);

            return new ResponseEntity<byte[]>(
                httpResponse.body().getBytes(),
                responseHeaders,
                valueOf(httpResponse.statusCode()));
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(e.getMessage().getBytes());
        }
    }

    @DeleteMapping("**")
    public ResponseEntity<Void> forwardDeleteRequests(HttpServletRequest request) throws InterruptedException {
        try {
            String requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            Builder requestBuilder = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath))).DELETE();
            transferRequestHeaders(request, requestBuilder);
            var httpResponse = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            var responseHeaders = copyResponseHeaders(httpResponse);

            return new ResponseEntity<>(responseHeaders, NO_CONTENT);
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return new ResponseEntity<>(INTERNAL_SERVER_ERROR);
        }
    }

    private static void transferRequestHeaders(HttpServletRequest request, Builder requestBuilder) {
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            if (!excluded(headerName)) {
                requestBuilder.header(headerName, request.getHeader(headerName));
            }
        });
    }

    private static LinkedMultiValueMap<String, String> copyResponseHeaders(HttpResponse<?> response) {
        var headers = new LinkedMultiValueMap<String, String>();

        response.headers().map()
            .forEach((key, values) -> {

                headers.add(key, String.join(";", values));
            });

        return headers;
    }

    private static boolean excluded(String headerName) {
        return "host".equalsIgnoreCase(headerName)
            || "connection".equalsIgnoreCase(headerName)
            || "accept-encoding".equalsIgnoreCase(headerName)
            || "content-length".equalsIgnoreCase(headerName);
    }

    private String getMockHttpServerUrl(String requestPath) {
        return "http://" + mockHttpServerHost + ":" + mockHttpServer.portNumber() + requestPath;
    }
}
