package moh.gov.zm.lis.messaging.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface InboundEventLogDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class InboundEventLogResponse {
        private UUID id;
        private String messageId;
        private String topic;
        private Short eventTypeId;
        private String eventTypeCode;
        private String sourceService;
        private UUID correlationId;
        private String processingStatus;
        private String errorMessage;
        private OffsetDateTime receivedAt;
        private OffsetDateTime processedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class RecordInboundEventRequest {
        @NotBlank
        private String messageId;
        private String topic;
        private String eventTypeCode;
        private String sourceService;
        private UUID correlationId;
        private String payload;
    }
}
