package zm.gov.moh.lisservice.lab.service;

import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.ListResponse;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.lab.dto.OrderStatusDTO;

import java.util.UUID;

public interface OrderStatusService {
    Mono<PagedResponse<OrderStatusDTO.OrderStatusResponse>> findAll(int page, int size);
    Mono<OrderStatusDTO.OrderStatusResponse> findById(UUID id);
    Mono<ListResponse<OrderStatusDTO.OrderStatusResponse>> findByOrderId(String orderId);
}
