package zm.gov.moh.lisservice.audit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.audit.dto.AuditLogsDTO;
import zm.gov.moh.lisservice.audit.service.AuditLogService;
import zm.gov.moh.lisservice.constant.ErrorResponse;
import zm.gov.moh.lisservice.constant.PagedResponse;

import java.time.LocalDateTime;
import java.util.UUID;

@Validated
@RestController
@RequestMapping(value = "api/v1/lis-service/audit-log", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Audit Log", description = "Audit Log API")
public class AuditLogController {
    private final AuditLogService auditLogService;

    @Operation(summary = "Get audit logs by organization ID", description = "Get audit logs by organization ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved audit logs by organization ID",
                    content = @Content(schema = @Schema(implementation = PagedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Audit logs not found for the given organization ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{organizationId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN_USER', 'ROLE_PORTAL_ADMIN')")
    public Mono<ResponseEntity<PagedResponse<AuditLogsDTO.AuditLogResponse>>> getAuditLogsByOrganizationId(
            @PathVariable UUID organizationId,
            @RequestParam(value = "keycloakUserId", required = false) String keycloakUserId,
            @RequestParam(value = "ipAddress", required = false) String ipAddress,
            @RequestParam(value = "userAgent", required = false) String userAgent,
            @RequestParam(value = "performedAt", required = false)LocalDateTime performedAt,
            @RequestParam(value = "sortBy", defaultValue = "performedAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") String sortDir,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size
    ) {
        AuditLogsDTO.SearchAuditLogs auditLogsRequest = AuditLogsDTO.SearchAuditLogs.builder()
                .keycloakUserId(keycloakUserId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .performedAt(performedAt)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .page(page)
                .size(size)
                .build();

        return auditLogService.getAuditLogsByOrganizationId(organizationId, auditLogsRequest)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
