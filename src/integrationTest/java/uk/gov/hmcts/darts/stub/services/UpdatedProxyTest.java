package uk.gov.hmcts.darts.stub.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UpdatedProxyTest extends IntegrationTestBase {

    private static final String PATH = "/api/v3/UpdateMetadata";
    private URI uri;

    @BeforeEach
    void createUri() throws URISyntaxException {
        uri = new URI(baseUrl + PATH);
    }

    @Test
    void testUpdate() {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.put("Content-Type", List.of("application/json"));
        HttpEntity<String> entity = new HttpEntity<>(
            "{\"UseGuidsForFields\":false,\"manifest\":{\"event_date\":\"2024-08-02T10:29:47.676644+01:00\"},\"itemId\":\"2\"}", headers);

        ResponseEntity<Map> result = this.restTemplate.postForEntity(uri, entity, Map.class);

        assertThat(result.getBody().get("itemId")).isEqualTo("7683ee65-c7a7-7343-be80-018b8ac13602");
        assertThat(result.getBody().get("cabinetId")).isEqualTo(101);
        assertThat(result.getBody().get("objectId")).isEqualTo("4bfe4fc7-4e2f-4086-8a0e-146cc4556260");
        assertThat(result.getBody().get("objectType")).isEqualTo(1);
        assertThat(result.getBody().get("fileName")).isEqualTo("UpdateMetadata-20241801-122819.json");
        assertThat(result.getBody().get("isError")).isEqualTo(false);
        assertThat(result.getBody().get("responseStatus")).isEqualTo(0);
        assertThat(result.getBody().get("responseStatusMessages")).isEqualTo(null);
        assertThat(result.getBody().get("retConfScore")).isEqualTo(10);
        assertThat(result.getBody().get("retConfReason")).isEqualTo("this is a mock reason");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}