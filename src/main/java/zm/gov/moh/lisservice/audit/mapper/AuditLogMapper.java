package zm.gov.moh.lisservice.audit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import zm.gov.moh.lisservice.audit.dto.AuditLogsDTO;
import zm.gov.moh.lisservice.audit.entity.AuditLogs;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AuditLogMapper {
    @Mapping(target = "id", ignore = true)
    AuditLogs toEntity(AuditLogsDTO.CreateAuditLog request);

    AuditLogsDTO.AuditLogResponse toResponse(AuditLogs auditLog);
}
