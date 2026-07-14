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

public interface FacilityLaboratoryMapDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Map a facility to a lab-offered test")
    class CreateFacilityLaboratoryMap {
        @NotNull
        private Long facilityId;
        @NotNull
        private UUID laboratoryTestId;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update a facility-to-lab-test mapping")
    class UpdateFacilityLaboratoryMap {
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Facility-to-lab-test mapping")
    class FacilityLaboratoryMapResponse {
        private UUID id;
        private Long facilityId;
        private UUID laboratoryTestId;
        private Boolean isActive;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }
}
