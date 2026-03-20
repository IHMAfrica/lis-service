package zm.gov.moh.lisservice.audit.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.audit.entity.AuditLogs;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditLogsRepository extends R2dbcRepository<AuditLogs, UUID> {
    @Query(
            "SELECT * FROM audit_logs WHERE " +
            "(:ip_address IS NULL OR LOWER(ip_address) LIKE LOWER(CONCAT('%', :ip_address, '%'))) " +
            "AND (:user_agent IS NULL OR LOWER(user_agent) LIKE LOWER(CONCAT('%', :user_agent, '%'))) " +
            "AND (:user_id IS NULL OR keycloak_user_id = :keycloakUserId) " +
            "AND (:performed_at IS NULL OR performed_at = :performed_at) " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'ipAddress' AND :sortDir = 'ASC' THEN ip_address END, " +
            "CASE WHEN :sortBy = 'ipAddress' AND :sortDir = 'DESC' THEN ip_address END DESC, " +
            "CASE WHEN :sortBy = 'userAgent' AND :sortDir = 'ASC' THEN user_agent END, " +
            "CASE WHEN :sortBy = 'userAgent' AND :sortDir = 'DESC' THEN user_agent END DESC, " +
            "CASE WHEN :sortBy = 'performedAt' AND :sortDir = 'ASC' THEN performed_at END, " +
            "CASE WHEN :sortBy = 'performedAt' AND :sortDir = 'DESC' THEN performed_at END DESC, " +
            "id " +
            "LIMIT :limit OFFSET :offset"
    )
    Flux<AuditLogs> findWithFilters(
            @Param("keycloakUserId") String keycloakUserId,
            @Param("ipAddress") String ipAddress,
            @Param("userAgent") String userAgent,
            @Param("performedAt") LocalDateTime performedAt,
            @Param("sortBy") String sortBy,
            @Param("sortDir") String sortDir,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    @Query(
            "SELECT COUNT(*) FROM audit_logs WHERE " +
            "(:ip_address IS NULL OR LOWER(ip_address) LIKE LOWER(CONCAT('%', :ip_address, '%'))) " +
            "AND (:user_agent IS NULL OR LOWER(user_agent) LIKE LOWER(CONCAT('%', :user_agent, '%'))) " +
            "AND (:user_id IS NULL OR keycloak_user_id = :keycloakUserId)" +
            "AND (:performed_at IS NULL OR performed_at = :performed_at)"
    )
    Mono<Long> countWithFilters(
            @Param("keycloakUserId") String keycloakUserId,
            @Param("ipAddress") String ipAddress,
            @Param("userAgent") String userAgent,
            @Param("performedAt") LocalDateTime performedAt
    );
}
