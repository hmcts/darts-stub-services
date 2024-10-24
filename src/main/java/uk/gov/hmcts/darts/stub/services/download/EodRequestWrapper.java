package uk.gov.hmcts.darts.stub.services.download;

import com.github.tomakehurst.wiremock.http.Request;

public class EodRequestWrapper {

    public static final String EOD_REQUEST_HEADER = "EOD_IDS";

    private Request request;

    EodRequestWrapper(Request request) {
        this.request = request;
    }

    public String[] extractEodFromHeader() {
        String eodHeaderValue =  request.getHeader(EOD_REQUEST_HEADER);

        // assumes the EOD header value are comma separated values but can be changed as appropriate
        return eodHeaderValue != null ? eodHeaderValue.split(",") : null;
    }
}