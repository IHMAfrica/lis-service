package moh.gov.zm.lis.lab.repository;

import moh.gov.zm.lis.lab.entity.OrderAckStatus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface OrderAckStatusRepository extends R2dbcRepository<OrderAckStatus, UUID> {
}
