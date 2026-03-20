package zm.gov.moh.lisservice.ref.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import zm.gov.moh.lisservice.ref.entity.Province;

@Repository
public interface ProvinceRepository extends R2dbcRepository<Province, Short> {
    Flux<Province> findAllBy(Pageable pageable);
}
