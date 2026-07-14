package moh.gov.zm.lis.lab.repository;

import moh.gov.zm.lis.lab.entity.FacilityLaboratoryMap;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

public interface FacilityLaboratoryMapRepository extends R2dbcRepository<FacilityLaboratoryMap, UUID> {
    Mono<Boolean> existsByFacilityIdAndLaboratoryTestId(Long facilityId, UUID laboratoryTestId);

    Flux<FacilityLaboratoryMap> findAllByFacilityIdIn(Collection<Long> facilityIds);
}
