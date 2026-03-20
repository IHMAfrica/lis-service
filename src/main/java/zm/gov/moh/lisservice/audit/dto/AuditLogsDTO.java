package zm.gov.moh.lisservice.audit.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import zm.gov.moh.lisservice.constant.Metadata;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AuditLogsDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateAuditLog {
        private String keycloakUserId;
        @NotNull(message = "Entity type is required")
        private String entityType;
        @NotNull(message = "Action is required")
        private String action;
        private Metadata.AuditLogsMetadata changes;
        private String reason;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime performedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class AuditLogRequest {
        private String keycloakUserId;
        private String ipAddress;
        private String userAgent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class SearchAuditLogs {
        private String keycloakUserId;
        private String ipAddress;
        private String userAgent;
        private LocalDateTime performedAt;
        private String sortBy;
        private String sortDir;
        private Integer page;
        private Integer size;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class AuditLogResponse {
        private UUID id;
        private String keycloakUserId;
        private String entityType;
        private String action;
        private Metadata.AuditLogsMetadata changes;
        private String reason;
        private String ipAddress;
        private String userAgent;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private LocalDateTime performedAt;
    }
}