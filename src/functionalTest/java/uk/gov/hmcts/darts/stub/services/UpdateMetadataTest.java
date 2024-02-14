package uk.gov.hmcts.darts.stub.services;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles({"dev", "functionalTest"})
class UpdateMetadataTest {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateMetadataTest.class);

    @BeforeEach
    void setUp() {
        configureRestAssured();
    }

    @Test
    void testUpdateMetadata() {
        String body = """
                {
                    "UseGuidsForFields": false,
                    "manifest": {"event_date": "2024-02-01T00:00:00.000Z"},
                    "itemId": "7683ee65-c7a7-7343-be80-018b8ac13602"
                }
                """;

        Response caseResponse = given()
                .contentType(ContentType.JSON)
                .when()
                .baseUri("https://darts-stub-services-pr-79.dev.platform.hmcts.net/api/v3/update-metadata")
                .body(body)
                .post()
                .then()
                .extract().response();

        assertEquals(200, caseResponse.statusCode());
        assertEquals("101", caseResponse.jsonPath().getString("cabinetId"));
    }

    private void configureRestAssured() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

}
