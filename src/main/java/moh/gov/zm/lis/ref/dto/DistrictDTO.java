package moh.gov.zm.lis.ref.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface DistrictDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "District")
    class DistrictResponse {
        @Schema(description = "District id", example = "42")
        private Long id;
        @Schema(description = "District name", example = "Kafue")
        private String name;
        @Schema(description = "Owning province id", example = "1")
        private Short provinceId;
        @Schema(description = "District code", example = "KAF")
        private String code;
    }
}
