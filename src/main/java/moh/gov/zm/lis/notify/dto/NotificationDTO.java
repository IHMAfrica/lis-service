package moh.gov.zm.lis.notify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface NotificationDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "A notification as seen by a recipient")
    class NotificationResponse {
        @Schema(description = "Notification id", example = "6f1e...")
        private UUID id;
        @Schema(description = "Notification kind", example = "LAB_RESULT")
        private String type;
        private String title;
        private String body;
        @Schema(description = "Optional JSON payload (as text)")
        private String data;
        private Long facilityId;
        private UUID correlationId;
        private OffsetDateTime createdAt;
        @Schema(description = "When this recipient read it; null = unread")
        private OffsetDateTime readAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create and dispatch a notification (to a facility's users or a single user)")
    class DispatchRequest {
        @Schema(description = "Target facility; notify all its active users")
        private Long facilityId;
        @Schema(description = "Target a single user directly (alternative to facilityId)")
        private UUID userId;
        @NotBlank
        @Size(max = 100)
        private String type;
        @Size(max = 255)
        private String title;
        private String body;
        @Schema(description = "Optional JSON payload (as text)")
        private String data;
        private UUID correlationId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Unread notification count for the current user")
    class UnreadCountResponse {
        private long unread;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Result of a bulk read operation")
    class MarkReadResult {
        private long updated;
    }
}
