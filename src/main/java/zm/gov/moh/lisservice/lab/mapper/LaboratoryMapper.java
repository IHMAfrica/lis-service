package zm.gov.moh.lisservice.lab.mapper;

import org.mapstruct.*;
import zm.gov.moh.lisservice.lab.dto.LaboratoryDTO;
import zm.gov.moh.lisservice.lab.entity.Laboratory;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface LaboratoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Laboratory toEntity(LaboratoryDTO.CreateLaboratory request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "labCode", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(LaboratoryDTO.UpdateLaboratory request, @MappingTarget Laboratory entity);

    LaboratoryDTO.LaboratoryResponse toResponse(Laboratory laboratory);
}
