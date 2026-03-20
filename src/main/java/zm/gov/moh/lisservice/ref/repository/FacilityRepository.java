package zm.gov.moh.lisservice.ref.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.ref.entity.Facility;

@Repository
public interface FacilityRepository extends R2dbcRepository<Facility, Long> {
    Flux<Facility> findByIsActiveTrue(Pageable pageable);
    Mono<Long> countByIsActiveTrue();
    Flux<Facility> findByDistrictIdAndIsActiveTrue(Long districtId, Pageable pageable);
    Mono<Long> countByDistrictIdAndIsActiveTrue(Long districtId);
    Mono<Facility> findByMflCodeAndIsActiveTrue(String mflCode);
    Mono<Facility> findByHmisCodeAndIsActiveTrue(String hmisCode);
}
