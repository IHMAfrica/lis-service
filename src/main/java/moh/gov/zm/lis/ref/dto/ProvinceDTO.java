package moh.gov.zm.lis.ref.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface ProvinceDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Province")
    class ProvinceResponse {
        @Schema(description = "Province id", example = "1")
        private Short id;
        @Schema(description = "Province name", example = "Lusaka")
        private String name;
        @Schema(description = "Province code", example = "LSK")
        private String code;
    }
}
