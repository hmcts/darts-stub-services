package uk.gov.hmcts.darts.stub.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WiremockProxyTest extends IntegrationTestBase {

    private static final String PATH = "/test";
    private URI uri;

    @BeforeEach
    void createUri() throws URISyntaxException {
        uri = new URI(baseUrl + PATH);
    }

    @Disabled
    @Test
    void forwardsGetRequests() {
        var result = this.restTemplate.getForEntity(uri, Map.class);

        assertThat(result.getBody().get("result")).isEqualTo("content");
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void forwardsPostRequests() {
        var result = this.restTemplate.postForEntity(uri, "{}", Map.class);

        assertThat(result.getBody().get("result")).isEqualTo("posted");
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Disabled
    @Test
    void forwardsPutRequests() {
        var result = this.restTemplate.exchange(uri, HttpMethod.PUT, null, Map.class);

        assertThat(result.getBody().get("result")).isEqualTo("putted");
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Disabled
    @Test
    void forwardsDeleteRequests() {
        var result = this.restTemplate.exchange(uri, HttpMethod.DELETE, null, Map.class);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}
