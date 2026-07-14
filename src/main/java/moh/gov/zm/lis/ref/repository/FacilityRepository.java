package moh.gov.zm.lis.ref.repository;

import moh.gov.zm.lis.ref.entity.Facility;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface FacilityRepository extends R2dbcRepository<Facility, Long> {
}
