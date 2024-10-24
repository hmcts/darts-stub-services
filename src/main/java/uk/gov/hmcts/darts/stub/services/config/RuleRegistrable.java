package uk.gov.hmcts.darts.stub.services.config;


import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/**
 * Allows new dynamic wiremock rules as well as extensions to be registered to wiremock.
 */
public interface RuleRegistrable {

    /**
     * Allows us to register dynamic rules on startup of the WireMock server.
     * @param server The server to register
     * @param config The configuration for the server
     */
    void register(WireMockServer server, WireMockConfiguration config);


    /**
     * register any transformer extensions.
     * @param configuration THE configuration to register the extension
     */
    void registerExtension(WireMockConfiguration configuration);
}