package zm.gov.moh.lisservice.lab.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import zm.gov.moh.lisservice.lab.dto.OrderStatusDTO;
import zm.gov.moh.lisservice.lab.entity.OrderStatus;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrderStatusMapper {
    OrderStatusDTO.OrderStatusResponse toResponse(OrderStatus orderStatus);
}
