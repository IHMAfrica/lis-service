package zm.gov.moh.lisservice.lab.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public interface LaboratoryTestDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateLaboratoryTest {
        @NotNull(message = "Laboratory ID is required")
        private Short laboratoryId;
        @NotNull(message = "Test ID is required")
        private Long testId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UpdateLaboratoryTest {
        @NotNull(message = "Laboratory ID is required")
        private Short laboratoryId;
        @NotNull(message = "Test ID is required")
        private Long testId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class LaboratoryTestResponse {
        private UUID id;
        private Short laboratoryId;
        private Long testId;
        private Boolean isActive;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private LocalDateTime createdAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private LocalDateTime updatedAt;
    }
}
