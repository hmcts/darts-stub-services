package uk.gov.hmcts.darts.stub.services.download;

import com.github.tomakehurst.wiremock.extension.ResponseTransformerV2;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wire mock transformer that rewrites the body with csv records according to the eods that exist in the request header.
 */
@Slf4j
public class CsvDownloadBodyTransformer implements ResponseTransformerV2 {
    public static final String TRANSFORMER_NAME = "csv_download_transformer";

    @Override
    public Response transform(Response response, ServeEvent serveEvent) {
        EodRequestWrapper headerExtractor = new EodRequestWrapper(serveEvent.getRequest());
        String[] eodHeader =  headerExtractor.extractEodFromHeader();
        Response returnResponse;
        try {
            // if we find no eod in the request then always return one back with the default eod
            if (eodHeader == null || eodHeader.length == 0) {
                returnResponse = Response.Builder.like(response)
                    .but().body(CsvUtil.convertToCsvString(
                        List.of(CsvDownloadFactory.getBasicCsvDownloadRecord(null))))
                    .build();
            } else {
                // if we do find multiple eods the process them as seperate csv records
                List<DownloadCsvRecord> records = new ArrayList<>();
                for (String eod: eodHeader) {
                    records.add(CsvDownloadFactory.getBasicCsvDownloadRecord(eod));
                }

                returnResponse = Response.Builder.like(response)
                    .but().body(CsvUtil.convertToCsvString(records))
                    .build();
            }
        } catch (CsvDataTypeMismatchException | CsvRequiredFieldEmptyException | IOException ex) {
            log.error("Something went wrong when transforming the body", ex);
            returnResponse = Response.Builder.like(response).body("Something went wrong %s".formatted(ex.getMessage()))
                .status(500).build();
        }

        return returnResponse;
    }

    @Override
    public String getName() {
        return TRANSFORMER_NAME;
    }

    @Override
    public boolean applyGlobally() {

        // do not apply this transformed everywhere
        return false;
    }
}