package zm.gov.moh.lisservice.lab.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.lab.entity.FacilityLaboratoryMap;

import java.util.UUID;

@Repository
public interface FacilityLaboratoryMapRepository extends R2dbcRepository<FacilityLaboratoryMap, UUID> {
    Flux<FacilityLaboratoryMap> findByIsActiveTrue(Pageable pageable);
    Mono<Long> countByIsActiveTrue();
    Flux<FacilityLaboratoryMap> findByFacilityIdAndIsActiveTrue(Long facilityId, Pageable pageable);
    Mono<Long> countByFacilityIdAndIsActiveTrue(Long facilityId);
    Mono<FacilityLaboratoryMap> findFirstByFacilityIdAndIsActiveTrue(Long facilityId);
}
