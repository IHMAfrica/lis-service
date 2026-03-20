package zm.gov.moh.lisservice.lab.mapper;

import org.mapstruct.*;
import zm.gov.moh.lisservice.lab.dto.TestDTO;
import zm.gov.moh.lisservice.lab.entity.Test;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Test toEntity(TestDTO.CreateTest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(TestDTO.UpdateTest request, @MappingTarget Test entity);

    TestDTO.TestResponse toResponse(Test test);
}
