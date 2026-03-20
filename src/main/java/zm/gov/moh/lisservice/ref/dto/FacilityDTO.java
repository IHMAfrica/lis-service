package zm.gov.moh.lisservice.ref.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    class FacilityResponse {
        private Long id;
        private String name;
        private Long districtId;
        private String hmisCode;
        private String mflCode;
        private Boolean isActive;
    }
}
