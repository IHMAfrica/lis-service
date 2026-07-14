package moh.gov.zm.lis.lab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

public interface TestDTO {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Create a test")
    class CreateTest {
        @NotBlank
        @Size(max = 256)
        private String name;
        @NotBlank
        @Size(max = 20)
        private String loincCode;
        @Size(max = 50)
        private String abbreviation;
        @Size(max = 50)
        private String shortTitle;
        private Boolean isCompositeTest;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Update a test")
    class UpdateTest {
        @Size(max = 256)
        private String name;
        @Size(max = 50)
        private String abbreviation;
        @Size(max = 50)
        private String shortTitle;
        private Boolean isCompositeTest;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Test")
    class TestResponse {
        private Long id;
        private String name;
        private String loincCode;
        private String abbreviation;
        private String shortTitle;
        private Boolean isCompositeTest;
        private Boolean isActive;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }
}
