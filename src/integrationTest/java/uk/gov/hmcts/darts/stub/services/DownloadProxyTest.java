package uk.gov.hmcts.darts.stub.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.darts.stub.services.download.CsvUtil;
import uk.gov.hmcts.darts.stub.services.download.DownloadCsvRecord;
import uk.gov.hmcts.darts.stub.services.download.EodRequestWrapper;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadProxyTest extends IntegrationTestBase {

    private static final String PATH = "/api/v1/downloadProduction/test/false";
    private URI uri;

    @BeforeEach
    void createUri() throws URISyntaxException {
        uri = new URI(baseUrl + PATH);
    }

    @Test
    void testDownloadWithEods() throws Exception {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.put("Content-Type", List.of("application/json"));

        Integer eodId1 = 100;
        Integer eodId2 = 291;
        Integer eodId3 = 341;

        headers.put(EodRequestWrapper.EOD_REQUEST_HEADER, List.of(List.of(eodId1.toString(), eodId2.toString(), eodId3.toString()).stream()
            .collect(Collectors.joining(", "))));

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<String> result = this.restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

        assertThat("application/csv").isEqualTo(result.getHeaders().get("Content-Type").get(0));

        List<DownloadCsvRecord> records = CsvUtil.toDownloadCsvRecord(result.getBody());
        Assertions.assertEquals(3, records.size());
        Assertions.assertEquals(Integer.toString(eodId1), records.get(0).getClientIdentifier());
        Assertions.assertEquals(Integer.toString(eodId2), records.get(1).getClientIdentifier());
        Assertions.assertEquals(Integer.toString(eodId3), records.get(2).getClientIdentifier());
        Assertions.assertFalse(anyNull(records.get(0)));
        Assertions.assertFalse(anyNull(records.get(1)));
        Assertions.assertFalse(anyNull(records.get(2)));
        assertAllOtherThanClientIdAreSame(CsvUtil.toDownloadCsvRecord(CsvUtil.getExpectedData()).get(0), records.get(0));
        assertAllOtherThanClientIdAreSame(CsvUtil.toDownloadCsvRecord(CsvUtil.getExpectedData()).get(0), records.get(1));
        assertAllOtherThanClientIdAreSame(CsvUtil.toDownloadCsvRecord(CsvUtil.getExpectedData()).get(0), records.get(2));
    }

    @Test
    void testDownloadWithOneEod() throws Exception {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.put("Content-Type", List.of("application/json"));
        headers.put(EodRequestWrapper.EOD_REQUEST_HEADER, List.of("100"));
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<String> result = this.restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        assertThat("application/csv").isEqualTo(result.getHeaders().get("Content-Type").get(0));
        List<DownloadCsvRecord> records = CsvUtil.toDownloadCsvRecord(result.getBody());
        Assertions.assertEquals(1, records.size());
        Assertions.assertEquals("100", records.get(0).getClientIdentifier());
        Assertions.assertFalse(anyNull(records.get(0)));
        assertAllOtherThanClientIdAreSame(CsvUtil.toDownloadCsvRecord(CsvUtil.getExpectedData()).get(0), records.get(0));
    }

    @Test
    void testDownloadWithNoEod() throws Exception {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.put("Content-Type", List.of("application/json"));
        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<String> result = this.restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        assertThat("application/csv").isEqualTo(result.getHeaders().get("Content-Type").get(0));
        List<DownloadCsvRecord> records = CsvUtil.toDownloadCsvRecord(result.getBody());
        Assertions.assertEquals(1, records.size());
        Assertions.assertEquals("Client Identifier", records.get(0).getClientIdentifier());
        Assertions.assertFalse(anyNull(records.get(0)));
        Assertions.assertEquals(CsvUtil.toDownloadCsvRecord(CsvUtil.getExpectedData()).get(0), records.get(0));
    }

    private void assertAllOtherThanClientIdAreSame(DownloadCsvRecord expected, DownloadCsvRecord actual) {
        expected.setClientIdentifier("Client Identifier");
        actual.setClientIdentifier("Client Identifier");
        Assertions.assertEquals(expected, actual);
    }

    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    public static boolean anyNull(Object target) {
        return Arrays.stream(target.getClass()
                                 .getDeclaredFields())
            .peek(f -> f.setAccessible(true))
            .map(f -> getFieldValue(f, target))
            .anyMatch(Objects::isNull);
    }

    private static Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }
}