package zm.gov.moh.lisservice.lab.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public interface TestDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateTest {
        @NotBlank(message = "Name is required")
        @Size(max = 256, message = "Name cannot exceed 256 characters")
        private String name;
        @Size(max = 20, message = "LOINC code cannot exceed 20 characters")
        private String loincCode;
        @Size(max = 50, message = "Abbreviation cannot exceed 50 characters")
        private String abbreviation;
        @Size(max = 50, message = "Short title cannot exceed 50 characters")
        private String shortTitle;
        private Boolean isCompositeTest;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UpdateTest {
        @NotBlank(message = "Name is required")
        @Size(max = 256, message = "Name cannot exceed 256 characters")
        private String name;
        @Size(max = 20, message = "LOINC code cannot exceed 20 characters")
        private String loincCode;
        @Size(max = 50, message = "Abbreviation cannot exceed 50 characters")
        private String abbreviation;
        @Size(max = 50, message = "Short title cannot exceed 50 characters")
        private String shortTitle;
        private Boolean isCompositeTest;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class TestResponse {
        private Long id;
        private String name;
        private String loincCode;
        private String abbreviation;
        private String shortTitle;
        private Boolean isCompositeTest;
        private Boolean isActive;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private LocalDateTime createdAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private LocalDateTime updatedAt;
    }
}
