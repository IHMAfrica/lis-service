package zm.gov.moh.lisservice.audit.service;

import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.audit.dto.AuditLogsDTO;
import zm.gov.moh.lisservice.constant.PagedResponse;

import java.util.UUID;

public interface AuditLogService {
    Mono<AuditLogsDTO.AuditLogResponse> createAuditLog(AuditLogsDTO.CreateAuditLog request);

    Mono<PagedResponse<AuditLogsDTO.AuditLogResponse>> getAuditLogsByOrganizationId(UUID organizationId, AuditLogsDTO.SearchAuditLogs request);
}
