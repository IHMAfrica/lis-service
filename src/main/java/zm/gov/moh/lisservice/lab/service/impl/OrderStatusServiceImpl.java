package zm.gov.moh.lisservice.lab.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import zm.gov.moh.lisservice.constant.ListResponse;
import zm.gov.moh.lisservice.constant.PagedResponse;
import zm.gov.moh.lisservice.exception.ResourceNotFoundException;
import zm.gov.moh.lisservice.lab.dto.OrderStatusDTO;
import zm.gov.moh.lisservice.lab.mapper.OrderStatusMapper;
import zm.gov.moh.lisservice.lab.repository.OrderStatusRepository;
import zm.gov.moh.lisservice.lab.service.OrderStatusService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderStatusRepository orderStatusRepository;
    private final OrderStatusMapper orderStatusMapper;

    @Override
    public Mono<PagedResponse<OrderStatusDTO.OrderStatusResponse>> findAll(int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate", "orderTime"));
        var content = orderStatusRepository.findAllBy(pageable).map(orderStatusMapper::toResponse);
        var total = orderStatusRepository.count();

        return Mono.zip(content.collectList(), total).map(tuple -> {
            var list = tuple.getT1();
            var totalCount = tuple.getT2();
            int totalPages = (int) Math.ceil((double) totalCount / size);
            return PagedResponse.<OrderStatusDTO.OrderStatusResponse>builder()
                    .content(list)
                    .totalElements(totalCount)
                    .totalPages(totalPages)
                    .currentPage(page)
                    .size(size)
                    .hasNext(page < totalPages - 1)
                    .hasPrevious(page > 0)
                    .build();
        });
    }

    @Override
    public Mono<OrderStatusDTO.OrderStatusResponse> findById(UUID id) {
        return orderStatusRepository.findById(id)
                .map(orderStatusMapper::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("OrderStatus", id.toString())));
    }

    @Override
    public Mono<ListResponse<OrderStatusDTO.OrderStatusResponse>> findByOrderId(String orderId) {
        return orderStatusRepository.findByOrderId(orderId)
                .map(orderStatusMapper::toResponse)
                .collectList()
                .map(list -> ListResponse.<OrderStatusDTO.OrderStatusResponse>builder()
                        .content(list)
                        .totalElements(list.size())
                        .build());
    }
}
