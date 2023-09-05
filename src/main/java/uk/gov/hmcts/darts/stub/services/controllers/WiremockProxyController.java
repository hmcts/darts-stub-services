package uk.gov.hmcts.darts.stub.services.controllers;

import io.micrometer.core.instrument.util.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Default endpoints per application.
 */
@RestController
@RequestMapping("/")
@SuppressWarnings("PMD.LawOfDemeter")
public class WiremockProxyController {

    private static final Logger LOG = LoggerFactory.getLogger(WiremockProxyController.class);
    static final String WIREMOCK_STUB_MAPPINGS_ENDPOINT = "/__admin/mappings";
    public static final String ERROR_OCCURRED = "Error occurred";

    @Value("${wiremock.server.host}")
    private String mockHttpServerHost;

    private final HttpClient httpClient;

    private final MockHttpServer mockHttpServer;

    public WiremockProxyController(HttpClient httpClient, MockHttpServer mockHttpServer) {
        this.httpClient = httpClient;
        this.mockHttpServer = mockHttpServer;
    }

    @GetMapping(value = "**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> forwardGetRequests(HttpServletRequest request) throws InterruptedException {
        try {
            String requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath)))
                .build();
            HttpResponse httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return new ResponseEntity<Object>(httpResponse.body().toString(),
                HttpStatus.valueOf(httpResponse.statusCode()));
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    @PostMapping(value = "**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> forwardPostRequests(HttpServletRequest request) throws InterruptedException {
        try {
            String requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            final String requestBody = IOUtils.toString(request.getInputStream());
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath)))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            HttpResponse httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return new ResponseEntity<Object>(httpResponse.body().toString(),
                HttpStatus.valueOf(httpResponse.statusCode()));
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    @PutMapping(value = "**", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> forwardPutRequests(HttpServletRequest request) throws InterruptedException {
        try {
            String requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            final String requestBody = IOUtils.toString(request.getInputStream());
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath)))
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            HttpResponse httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return new ResponseEntity<Object>(httpResponse.body().toString(),
                HttpStatus.valueOf(httpResponse.statusCode()));
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    @DeleteMapping("**")
    public ResponseEntity<Object> forwardDeleteRequests(HttpServletRequest request) throws InterruptedException {
        try {
            String requestPath = new AntPathMatcher().extractPathWithinPattern("**", request.getRequestURI());
            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create(getMockHttpServerUrl(requestPath)))
                .DELETE()
                .build();
            HttpResponse httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return new ResponseEntity<Object>(httpResponse.body().toString(),
                HttpStatus.valueOf(httpResponse.statusCode()));
        } catch (IOException e) {
            LOG.error(ERROR_OCCURRED, e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.getMessage());
        }
    }

    private String getMockHttpServerUrl(String requestPath) {
        return "http://" + mockHttpServerHost + ":" + mockHttpServer.portNumber() + requestPath;
    }
}
