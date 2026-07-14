package moh.gov.zm.lis.lab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

public interface LaboratoryDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create a laboratory")
    class CreateLaboratory {
        @NotBlank
        @Size(max = 5)
        private String labCode;
        @NotBlank
        @Size(max = 100)
        private String labName;
        @NotNull
        private Long districtId;
        @Size(max = 50)
        private String comment;
        private Short labTypeId;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update a laboratory")
    class UpdateLaboratory {
        @Size(max = 100)
        private String labName;
        private Long districtId;
        @Size(max = 50)
        private String comment;
        private Short labTypeId;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Laboratory")
    class LaboratoryResponse {
        private Short id;
        private String labCode;
        private String labName;
        private Long districtId;
        private String comment;
        private Short labTypeId;
        private Boolean isActive;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }
}
