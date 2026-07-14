package moh.gov.zm.lis.ref.repository;

import moh.gov.zm.lis.ref.entity.District;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface DistrictRepository extends R2dbcRepository<District, Long> {
}
