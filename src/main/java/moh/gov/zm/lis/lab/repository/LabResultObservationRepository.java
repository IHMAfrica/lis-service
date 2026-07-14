package moh.gov.zm.lis.lab.repository;

import moh.gov.zm.lis.lab.entity.LabResultObservation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface LabResultObservationRepository extends R2dbcRepository<LabResultObservation, UUID> {
    Flux<LabResultObservation> findAllByLabResultIdOrderBySetId(UUID labResultId);
}
