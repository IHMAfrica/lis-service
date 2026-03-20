package zm.gov.moh.lisservice.constant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface Metadata {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class AuditLogsMetadata {
        String before;
        String after;
    }
}
