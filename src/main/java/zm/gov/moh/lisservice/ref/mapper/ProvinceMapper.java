package zm.gov.moh.lisservice.ref.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import zm.gov.moh.lisservice.ref.dto.ProvinceDTO;
import zm.gov.moh.lisservice.ref.entity.Province;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProvinceMapper {
    ProvinceDTO.ProvinceResponse toResponse(Province province);
}
