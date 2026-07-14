package moh.gov.zm.lis.lab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public interface BulkUploadDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Summary of a facility-laboratory-map bulk upload")
    class BulkUploadSummary {
        @Schema(description = "Total data rows read from the sheet (excludes the header and blank rows)", example = "120")
        private int totalRows;
        @Schema(description = "New mappings created", example = "80")
        private int created;
        @Schema(description = "Existing inactive mappings reactivated", example = "5")
        private int reactivated;
        @Schema(description = "Existing active mappings left unchanged", example = "30")
        private int skippedExisting;
        @Schema(description = "Rows that duplicated an earlier row in the same file", example = "2")
        private int duplicateRows;
        @Schema(description = "Rows that could not be processed", example = "3")
        private int errorRows;
        @Schema(description = "Details of each row that failed")
        private List<RowError> errors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "A single row that could not be processed")
    class RowError {
        @Schema(description = "1-based row number in the sheet (matches the spreadsheet)", example = "14")
        private int row;
        private String mflCode;
        private String loincCode;
        private String labCode;
        @Schema(description = "Why the row was rejected", example = "Unknown mfl code")
        private String reason;
    }
}
