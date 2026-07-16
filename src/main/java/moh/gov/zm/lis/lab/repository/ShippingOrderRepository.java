package moh.gov.zm.lis.lab.repository;

import moh.gov.zm.lis.lab.entity.ShippingOrder;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ShippingOrderRepository extends R2dbcRepository<ShippingOrder, UUID> {
    /** Orders shipped from a facility to a lab within the received-time window, oldest first. */
    Flux<ShippingOrder> findByMflCodeAndLabCodeAndCreatedAtBetweenOrderByCreatedAtAsc(
            String mflCode, String labCode, OffsetDateTime from, OffsetDateTime to);
}
