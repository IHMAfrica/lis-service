package zm.gov.moh.lisservice.audit.util;


import zm.gov.moh.lisservice.audit.dto.AuditLogsDTO;
import zm.gov.moh.lisservice.constant.Metadata;

import java.time.LocalDateTime;

public class AuditUtil {
    public static AuditLogsDTO.CreateAuditLog createAuditLog(
            AuditLogsDTO.AuditLogRequest auditLogRequest,
            Metadata.AuditLogsMetadata changes,
            String entityType,
            String action
    ) {
        return AuditLogsDTO.CreateAuditLog.builder()
                .keycloakUserId(auditLogRequest.getKeycloakUserId())
                .entityType(entityType)
                .action(action)
                .changes(changes)
                .ipAddress(auditLogRequest.getIpAddress())
                .userAgent(auditLogRequest.getUserAgent())
                .performedAt(LocalDateTime.now())
                .build();
    }
}
