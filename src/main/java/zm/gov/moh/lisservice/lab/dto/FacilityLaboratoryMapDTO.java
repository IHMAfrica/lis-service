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

public interface FacilityLaboratoryMapDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateFacilityLaboratoryMap {
        @NotNull(message = "Facility ID is required")
        private Long facilityId;
        @NotNull(message = "Laboratory test ID is required")
        private UUID laboratoryTestId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UpdateFacilityLaboratoryMap {
        @NotNull(message = "Facility ID is required")
        private Long facilityId;
        @NotNull(message = "Laboratory test ID is required")
        private UUID laboratoryTestId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class FacilityLaboratoryMapResponse {
        private UUID id;
        private Long facilityId;
        private UUID laboratoryTestId;
        private Boolean isActive;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private LocalDateTime createdAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private LocalDateTime updatedAt;
    }
}
