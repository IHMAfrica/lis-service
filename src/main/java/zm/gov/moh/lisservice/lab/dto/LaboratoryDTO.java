package zm.gov.moh.lisservice.lab.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public interface LaboratoryDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class CreateLaboratory {
        @NotBlank(message = "Lab code is required")
        @Size(max = 5, message = "Lab code cannot exceed 5 characters")
        private String labCode;
        @NotBlank(message = "Lab name is required")
        @Size(max = 100, message = "Lab name cannot exceed 100 characters")
        private String labName;
        @NotNull(message = "District ID is required")
        private Long districtId;
        @Size(max = 50, message = "Comment cannot exceed 50 characters")
        private String comment;
        private Short labTypeId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UpdateLaboratory {
        @NotBlank(message = "Lab name is required")
        @Size(max = 100, message = "Lab name cannot exceed 100 characters")
        private String labName;
        @NotNull(message = "District ID is required")
        private Long districtId;
        @Size(max = 50, message = "Comment cannot exceed 50 characters")
        private String comment;
        private Short labTypeId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class LaboratoryResponse {
        private Short id;
        private String labCode;
        private String labName;
        private Long districtId;
        private String comment;
        private Short labTypeId;
        private Boolean isActive;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private LocalDateTime createdAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        private LocalDateTime updatedAt;
    }
}
