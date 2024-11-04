package uk.gov.hmcts.darts.stub.services.download;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;


@Slf4j
public class CsvDownloadFactory {

    private static String EXPECTED_DATA;

    static {
        try {
            EXPECTED_DATA = CsvUtil.getExpectedData();
        } catch (IOException ioException) {
            log.error("Unable to read the expected data", ioException);
            throw new AssertionError("Unable to read the expected data", ioException);
        }
    }

    private CsvDownloadFactory() {
    }

    public static DownloadCsvRecord getBasicCsvDownloadRecord(String eodId) throws IOException {
        List<DownloadCsvRecord> downloadCsvRecord = CsvUtil.toDownloadCsvRecord(EXPECTED_DATA);
        DownloadCsvRecord csv;
        if (downloadCsvRecord.size() == 1) {
            csv = downloadCsvRecord.get(0);

            // if the eod is present set it
            if (eodId != null && !eodId.isBlank()) {
                csv.setClientIdentifier(eodId.trim());
            }
        } else {
            throw new IOException("No records found in the expected data");
        }

        return csv;
    }
}