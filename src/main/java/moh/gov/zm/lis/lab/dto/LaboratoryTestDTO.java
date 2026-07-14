package moh.gov.zm.lis.lab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface LaboratoryTestDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Register a test as offered by a laboratory")
    class CreateLaboratoryTest {
        @NotNull
        private Short laboratoryId;
        @NotNull
        private Long testId;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update a laboratory-test offering")
    class UpdateLaboratoryTest {
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Laboratory-test offering")
    class LaboratoryTestResponse {
        private UUID id;
        private Short laboratoryId;
        private Long testId;
        private Boolean isActive;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }
}
