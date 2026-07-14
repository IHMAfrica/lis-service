package moh.gov.zm.lis.notify.repository;

import moh.gov.zm.lis.notify.entity.NotificationRecipient;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

import java.util.UUID;

public interface NotificationRecipientRepository extends R2dbcRepository<NotificationRecipient, UUID> {
}
