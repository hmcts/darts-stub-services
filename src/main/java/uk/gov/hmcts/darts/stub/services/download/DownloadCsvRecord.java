package uk.gov.hmcts.darts.stub.services.download;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class DownloadCsvRecord {
    @CsvBindByPosition(position = 0)
    private String record;

    @CsvBindByPosition(position = 1)
    private String archiveDate;

    @CsvBindByPosition(position = 2)
    private String clientIdentifier;

    @CsvBindByPosition(position = 3)
    private String contributor;

    @CsvBindByPosition(position = 4)
    private String recordDate;

    @CsvBindByPosition(position = 5)
    private String object;

    @CsvBindByPosition(position = 6)
    private String parentId;

    @CsvBindByPosition(position = 7)
    private String channel;

    @CsvBindByPosition(position = 8)
    private String maxChannel;

    @CsvBindByPosition(position = 9)
    private String objectType;

    @CsvBindByPosition(position = 10)
    private String caseNumbers;

    @CsvBindByPosition(position = 11)
    private String fileType;

    @CsvBindByPosition(position = 12)
    private String checksum;

    @CsvBindByPosition(position = 13)
    private String transcriptRequest;

    @CsvBindByPosition(position = 14)
    private String transcriptionType;

    @CsvBindByPosition(position = 15)
    private String transcriptUrgency;

    @CsvBindByPosition(position = 16)
    private String comments;

    @CsvBindByPosition(position = 17)
    private String uploadedBy;

    @CsvBindByPosition(position = 18)
    private String hearingDate;

    @CsvBindByPosition(position = 19)
    private String createdDate;

    @CsvBindByPosition(position = 20)
    private String startTime;

    @CsvBindByPosition(position = 21)
    private String endTime;

    @CsvBindByPosition(position = 22)
    private String courthouse;

    @CsvBindByPosition(position = 23)
    private String courtroom;

}