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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

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

    public WiremockRequestForwardingController(RestTemplate restTemplate, MockHttpServer mockHttpServer) {
        this.restTemplate = restTemplate;
        this.mockHttpServer = mockHttpServer;
    }

    @GetMapping("**")
    public ResponseEntity<String> forwardGetRequests(HttpServletRequest request) {
        return forwardRequestForMethod(request, GET);
    }


    @PostMapping("**")
    public ResponseEntity<String> forwardPostRequests(HttpServletRequest request) {
        return forwardRequestForMethod(request, POST);
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
