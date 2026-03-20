package zm.gov.moh.lisservice.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import zm.gov.moh.lisservice.constant.Metadata;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "audit", name = "audit_logs")
public class AuditLogs {
    @Id
    private UUID id;
    private String keycloakUserId;
    private String entityType;
    private String action;
    private Metadata.AuditLogsMetadata changes;
    private String reason;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime performedAt;
}