package zm.gov.moh.lisservice.lab.mapper;

import org.mapstruct.*;
import zm.gov.moh.lisservice.lab.dto.FacilityLaboratoryMapDTO;
import zm.gov.moh.lisservice.lab.entity.FacilityLaboratoryMap;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FacilityLaboratoryMapMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FacilityLaboratoryMap toEntity(FacilityLaboratoryMapDTO.CreateFacilityLaboratoryMap request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(FacilityLaboratoryMapDTO.UpdateFacilityLaboratoryMap request, @MappingTarget FacilityLaboratoryMap entity);

    FacilityLaboratoryMapDTO.FacilityLaboratoryMapResponse toResponse(FacilityLaboratoryMap facilityLaboratoryMap);
}
