package moh.gov.zm.lis.ref.repository;

import moh.gov.zm.lis.ref.entity.ReferenceData;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Mono;

/**
 * Base repository shared by all {@code ref} lookup tables. Concrete lookup
 * repositories simply extend this with their entity type.
 */
@NoRepositoryBean
public interface ReferenceDataRepository<E extends ReferenceData> extends R2dbcRepository<E, Short> {
    Mono<E> findByCode(String code);

    Mono<Boolean> existsByCode(String code);
}
