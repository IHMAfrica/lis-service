package moh.gov.zm.lis.lab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

public interface LabStatisticsDTO {
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Basic lab-workflow statistics for a facility over a time period")
    class StatisticsResponse {
        @Schema(description = "Facility MFL code the statistics are scoped to", example = "101010")
        private String mflCode;

        @Schema(description = "Facility name, when the MFL code resolves to a known facility")
        private String facilityName;

        @Schema(description = "Start of the reporting window (inclusive)", example = "2026-06-14")
        private LocalDate from;

        @Schema(description = "End of the reporting window (inclusive)", example = "2026-07-13")
        private LocalDate to;

        @Schema(description = "Lab orders enqueued for publishing by this facility")
        private long labOrdersSent;

        @Schema(description = "Lab orders that received an acknowledgement from the laboratory")
        private long labOrdersAcknowledged;

        @Schema(description = "Total lab result messages received for this facility")
        private long labResultsReceived;

        @Schema(description = "Received results carrying valid observations (message_kind = RESULT)")
        private long labResultsValid;

        @Schema(description = "Received results that could not be matched to a known order (unsolicited)")
        private long labResultsUnsolicited;
    }
}
