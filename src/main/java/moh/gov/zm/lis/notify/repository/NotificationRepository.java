package moh.gov.zm.lis.notify.repository;

import moh.gov.zm.lis.notify.entity.Notification;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface NotificationRepository extends R2dbcRepository<Notification, UUID> {
}
