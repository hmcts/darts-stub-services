package uk.gov.hmcts.darts.stub.services.server;

public interface MockHttpServer {

    void start();

    void stop();

    int portNumber();
}
