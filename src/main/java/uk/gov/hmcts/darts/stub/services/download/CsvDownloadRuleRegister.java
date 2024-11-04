package uk.gov.hmcts.darts.stub.services.download;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Extension;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.darts.stub.services.config.RuleRegistrable;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

@Component
public class CsvDownloadRuleRegister implements RuleRegistrable {

    @Override
    public void register(WireMockServer wireMockServer, WireMockConfiguration configuration) {
        wireMockServer.stubFor(get(urlPathMatching("/api/v1/downloadProduction/([A-Za-z\\-]+)/false"))
                                   .withHeader("Content-type", equalTo("application/json"))
                                   .willReturn(aResponse().withTransformers(CsvDownloadBodyTransformer.TRANSFORMER_NAME)
                                                   .withHeader("Content-Type", "application/csv")
                                                   .withStatus(200)));
    }

    @Override
    public void registerExtension(WireMockConfiguration configuration) {
        configuration.extensions(getTransformer().getCanonicalName());
    }

    private Class<? extends Extension> getTransformer() {
        return CsvDownloadBodyTransformer.class;
    }
}