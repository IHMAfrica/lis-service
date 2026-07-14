package moh.gov.zm.lis.messaging.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface OutboundEventOutboxDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class OutboundEventOutboxResponse {
        private UUID id;
        private Short eventTypeId;
        private String eventTypeCode;
        private String eventTypeName;
        private String topic;
        private UUID correlationId;
        private Boolean isPublished;
        private OffsetDateTime publishedAt;
        private Short retryCount;
        private String lastError;
        private OffsetDateTime createdAt;
        private UUID createdBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateOutboundEventOutboxRequest {
        private Short eventTypeId;
        private String topic;
        private String payload;
        private UUID correlationId;
        private UUID createdBy;
    }
}
