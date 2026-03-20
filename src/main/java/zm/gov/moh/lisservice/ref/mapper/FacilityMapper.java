package zm.gov.moh.lisservice.ref.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import zm.gov.moh.lisservice.ref.dto.FacilityDTO;
import zm.gov.moh.lisservice.ref.entity.Facility;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FacilityMapper {
    FacilityDTO.FacilityResponse toResponse(Facility facility);
}
