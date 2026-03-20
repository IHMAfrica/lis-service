package zm.gov.moh.lisservice.ref.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.ref.entity.District;

@Repository
public interface DistrictRepository extends R2dbcRepository<District, Long> {
    Flux<District> findAllBy(Pageable pageable);
    Flux<District> findByProvinceId(Short provinceId, Pageable pageable);
    Mono<Long> countByProvinceId(Short provinceId);
}
