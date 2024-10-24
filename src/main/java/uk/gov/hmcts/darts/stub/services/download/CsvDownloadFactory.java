package uk.gov.hmcts.darts.stub.services.download;


import java.io.IOException;
import java.util.List;


public class CsvDownloadFactory {

    private CsvDownloadFactory() {
    }

    public static DownloadCsvRecord getBasicCsvDownloadRecord(String eodId) throws IOException {
        List<DownloadCsvRecord> downloadCsvRecord = CsvUtil.toDownloadCsvRecord(CsvUtil.getExpectedData());
        DownloadCsvRecord csv;
        if (downloadCsvRecord.size() == 1) {
            csv = downloadCsvRecord.get(0);

            // if the eod is present set it
            if (eodId != null) {
                csv.setClientIdentifier(eodId.trim());
            }
        } else {
            throw new IOException("No records found in the expected data");
        }

        return csv;
    }
}