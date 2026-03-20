package zm.gov.moh.lisservice.lab.mapper;

import org.mapstruct.*;
import zm.gov.moh.lisservice.lab.dto.LabTypeDTO;
import zm.gov.moh.lisservice.lab.entity.LabType;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface LabTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LabType toEntity(LabTypeDTO.CreateLabType request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(LabTypeDTO.UpdateLabType request, @MappingTarget LabType entity);

    LabTypeDTO.LabTypeResponse toResponse(LabType labType);
}
