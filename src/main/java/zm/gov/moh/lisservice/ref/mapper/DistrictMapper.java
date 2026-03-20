package zm.gov.moh.lisservice.ref.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import zm.gov.moh.lisservice.ref.dto.DistrictDTO;
import zm.gov.moh.lisservice.ref.entity.District;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DistrictMapper {
    DistrictDTO.DistrictResponse toResponse(District district);
}
