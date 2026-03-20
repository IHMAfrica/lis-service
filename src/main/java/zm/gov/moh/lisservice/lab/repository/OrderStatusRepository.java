package zm.gov.moh.lisservice.lab.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import zm.gov.moh.lisservice.lab.entity.OrderStatus;

import java.util.UUID;

@Repository
public interface OrderStatusRepository extends R2dbcRepository<OrderStatus, UUID> {
    Flux<OrderStatus> findAllBy(Pageable pageable);
    Flux<OrderStatus> findByOrderId(String orderId);
}
