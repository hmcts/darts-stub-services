package uk.gov.hmcts.darts.stub.services.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Configuration
public class WireMockServerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(WireMockServerConfig.class);
    private static final String MAPPINGS_DIRECTORY_NAME = "/mappings";

    private final String mappingsPath;

    @Autowired
    public WireMockServerConfig(@Value("${wiremock.server.mappings-path}") String mappingsPath) {
        this.mappingsPath = mappingsPath;
    }

    @Autowired
    private List<RuleRegistrable> dynamicRules;

    @Bean
    public WireMockServer wireMockServer() {
        LOG.info("WireMock mappings file path: {}", mappingsPath);

        WireMockConfiguration configuration = getWireMockConfig();
        dynamicRules.stream().forEach(rule -> rule.registerExtension(configuration));

        WireMockServer wireMockServer = new WireMockServer(configuration);

        dynamicRules.stream().forEach(rule -> rule.register(wireMockServer, configuration));

        LOG.info("Stubs registered with wiremock");
        wireMockServer.getStubMappings().forEach(w -> LOG.info("\nRequest : {}, \nResponse: {}", w.getRequest(),
                                                               w.getResponse()
        ));

        return wireMockServer;
    }

    private WireMockConfiguration getWireMockConfig() {
        File mappingDirectory = new File(mappingsPath + MAPPINGS_DIRECTORY_NAME);
        LOG.info("Mappings directory path: {}", mappingDirectory.getAbsolutePath());

        if (mappingDirectory.isDirectory()) {
            return options()
                    .stubCorsEnabled(false)
                    .dynamicHttpsPort()
                    .dynamicPort()
                    .usingFilesUnderDirectory(mappingsPath)
                    .globalTemplating(false);
        } else {
            LOG.info("using classpath resources to resolve mappings");
            return options()
                    .stubCorsEnabled(false)
                    .dynamicHttpsPort()
                    .dynamicPort()
                    .usingFilesUnderClasspath(mappingsPath)
                    .globalTemplating(false);
        }
    }


}