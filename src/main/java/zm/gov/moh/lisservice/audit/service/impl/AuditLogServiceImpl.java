package zm.gov.moh.lisservice.audit.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.audit.dto.AuditLogsDTO;
import zm.gov.moh.lisservice.audit.entity.AuditLogs;
import zm.gov.moh.lisservice.audit.mapper.AuditLogMapper;
import zm.gov.moh.lisservice.audit.repository.AuditLogsRepository;
import zm.gov.moh.lisservice.audit.service.AuditLogService;
import zm.gov.moh.lisservice.constant.PagedResponse;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogMapper auditLogMapper;
    private final AuditLogsRepository auditLogsRepository;

    @Override
    @Transactional
    public Mono<AuditLogsDTO.AuditLogResponse> createAuditLog(AuditLogsDTO.CreateAuditLog request) {
        AuditLogs entity = auditLogMapper.toEntity(request);
        entity.setPerformedAt(LocalDateTime.now());

        return auditLogsRepository.save(entity)
                .map(auditLogMapper::toResponse);
    }

    @Override
    public Mono<PagedResponse<AuditLogsDTO.AuditLogResponse>> getAuditLogsByOrganizationId(
            UUID organizationId,
            AuditLogsDTO.SearchAuditLogs request
    ) {
        String sortBy = validateSortBy(request.getSortBy());
        String sortDir = validateSortDir(request.getSortDir());
        int offset = request.getPage() * request.getSize();

        Mono<Long> totalCount = auditLogsRepository.countWithFilters(
                request.getKeycloakUserId(),
                request.getIpAddress(),
                request.getUserAgent(),
                request.getPerformedAt()
        );

        Flux<AuditLogsDTO.AuditLogResponse> auditLogs = auditLogsRepository.findWithFilters(
                request.getKeycloakUserId(),
                request.getIpAddress(),
                request.getUserAgent(),
                request.getPerformedAt(),
                sortBy,
                sortDir,
                request.getSize(),
                offset
        ).map(auditLogMapper::toResponse);

        return Mono.zip(auditLogs.collectList(), totalCount)
                .map(tuple -> {
                    var content = tuple.getT1();
                    var totalElements = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

                    return PagedResponse.<AuditLogsDTO.AuditLogResponse>builder()
                            .content(content)
                            .totalElements(totalElements)
                            .totalPages(totalPages)
                            .currentPage(request.getPage())
                            .size(request.getSize())
                            .hasNext(request.getPage() < totalPages - 1)
                            .hasPrevious(request.getPage() > 0)
                            .build();
                });
    }

    private String validateSortBy(String sortBy) {
        if (sortBy == null) return "ipAddress";

        return switch (sortBy.toLowerCase()) {
            case "ipaddress", "ip_address" -> "ipAddress";
            case "useragent", "user_agent" -> "userAgent";
            default -> "performedAt";
        };
    }

    private String validateSortDir(String sortDir) {
        if (sortDir == null) return "ASC";

        return sortDir.equalsIgnoreCase("DESC") ? "DESC" : "ASC";
    }
}
