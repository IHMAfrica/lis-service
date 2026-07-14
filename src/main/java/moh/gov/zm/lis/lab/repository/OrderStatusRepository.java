package moh.gov.zm.lis.lab.repository;

import moh.gov.zm.lis.lab.entity.OrderStatus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderStatusRepository extends R2dbcRepository<OrderStatus, UUID> {
    /** Matches a lab-order acknowledgement (MSA-2) back to its originating order. */
    Mono<OrderStatus> findByMessageControlId(String messageControlId);

    /** Primary lab-result reconciliation: match the placer order number to our order id. */
    Mono<OrderStatus> findFirstByOrderIdOrderByCreatedAtDesc(String orderId);
}
