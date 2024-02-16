package uk.gov.hmcts.darts.stub.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"uk.gov.hmcts.darts.stub.services.*"})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "Content-Length");
        SpringApplication.run(Application.class, args);
    }
}
