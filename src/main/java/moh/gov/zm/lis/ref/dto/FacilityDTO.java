package moh.gov.zm.lis.ref.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface FacilityDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Health facility")
    class FacilityResponse {
        @Schema(description = "Facility id", example = "1001")
        private Long id;
        @Schema(description = "Facility name", example = "University Teaching Hospital")
        private String name;
        @Schema(description = "District id", example = "42")
        private Long districtId;
        @Schema(description = "HMIS code", example = "504010")
        private String hmisCode;
        @Schema(description = "Master Facility List code", example = "1010101")
        private String mflCode;
        @Schema(description = "Whether the facility is active", example = "true")
        private Boolean isActive;
    }
}
