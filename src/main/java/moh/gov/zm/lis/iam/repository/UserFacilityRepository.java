package moh.gov.zm.lis.iam.repository;

import moh.gov.zm.lis.iam.entity.UserFacility;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserFacilityRepository extends R2dbcRepository<UserFacility, Long> {
    /** Active users assigned to a facility (both the assignment and the user must be active). */
    @Query("""
            SELECT uf.user_id
            FROM iam.user_facility uf
            JOIN iam.users u ON u.user_id = uf.user_id
            WHERE uf.facility_id = :facilityId AND uf.is_active AND u.is_active
            """)
    Flux<UUID> findActiveUserIdsByFacilityId(Long facilityId);

    Mono<Boolean> existsByUserIdAndFacilityIdAndIsActiveTrue(UUID userId, Long facilityId);
}
