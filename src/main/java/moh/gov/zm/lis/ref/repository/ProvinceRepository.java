package moh.gov.zm.lis.ref.repository;

import moh.gov.zm.lis.ref.entity.Province;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface ProvinceRepository extends R2dbcRepository<Province, Short> {
}
