package moh.gov.zm.lis.ref.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

public interface ReferenceDataDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateReferenceData {
        @NotBlank
        @Size(max = 100)
        private String code;
        @NotBlank
        @Size(max = 150)
        private String name;
        @Size(max = 500)
        private String description;
        private Boolean isActive;
        private Short sortOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UpdateReferenceData {
        @Size(max = 150)
        private String name;
        @Size(max = 500)
        private String description;
        private Boolean isActive;
        private Short sortOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class SearchReferenceDataParams {
        private String code;
        private String name;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class SearchReferenceData {
        private SearchReferenceDataParams params;
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
    class ReferenceDataResponse {
        private Short id;
        private String code;
        private String name;
        private String description;
        private Boolean isActive;
        private Short sortOrder;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private OffsetDateTime createdAt;
    }
}
