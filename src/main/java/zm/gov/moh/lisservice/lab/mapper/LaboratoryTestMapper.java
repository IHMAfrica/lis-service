package zm.gov.moh.lisservice.lab.mapper;

import org.mapstruct.*;
import zm.gov.moh.lisservice.lab.dto.LaboratoryTestDTO;
import zm.gov.moh.lisservice.lab.entity.LaboratoryTest;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface LaboratoryTestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LaboratoryTest toEntity(LaboratoryTestDTO.CreateLaboratoryTest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(LaboratoryTestDTO.UpdateLaboratoryTest request, @MappingTarget LaboratoryTest entity);

    LaboratoryTestDTO.LaboratoryTestResponse toResponse(LaboratoryTest laboratoryTest);
}
