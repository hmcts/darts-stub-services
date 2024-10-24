package uk.gov.hmcts.darts.stub.services.download;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import wiremock.org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

public class CsvUtil {

    private CsvUtil() {

    }

    public static List<DownloadCsvRecord> toDownloadCsvRecord(String csvString) {

        CsvToBean<DownloadCsvRecord> cb = new CsvToBeanBuilder<DownloadCsvRecord>(new StringReader(csvString))
            .withType(DownloadCsvRecord.class)
            .build();

        return cb.stream().toList();
    }

    public static String getExpectedData() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return IOUtils.toString(Objects.requireNonNull(classLoader
                                                           .getResourceAsStream("expectedDownload.csv")), Charset.defaultCharset());
    }

    public static String convertToCsvString(List<DownloadCsvRecord> downloadCsv) throws CsvDataTypeMismatchException,
        CsvRequiredFieldEmptyException, IOException {
        try (StringWriter writer = new StringWriter()) {
            var builder = new StatefulBeanToCsvBuilder<DownloadCsvRecord>(writer)
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .withSeparator(',')
                .build();

            // write all of the csv records
            for (DownloadCsvRecord csv : downloadCsv) {
                builder.write(csv);
            }

            return writer.toString();
        }
    }
}