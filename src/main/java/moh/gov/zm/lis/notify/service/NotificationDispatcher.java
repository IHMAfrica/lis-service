package moh.gov.zm.lis.notify.service;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.notify.dto.NotificationDTO;
import moh.gov.zm.lis.notify.entity.Notification;
import moh.gov.zm.lis.notify.entity.NotificationRecipient;
import moh.gov.zm.lis.notify.repository.NotificationRecipientRepository;
import moh.gov.zm.lis.notify.repository.NotificationRepository;
import moh.gov.zm.lis.notify.sse.NotificationBroadcaster;
import moh.gov.zm.lis.iam.repository.UserFacilityRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Creates notifications and fans them out. A notification is persisted once, a
 * per-user recipient row is written for each target, and a single message is
 * published to Redis so every instance can push it to its live SSE clients.
 *
 * <p>This is the entry point the future lab-order-ack / lab-result handlers will
 * call ({@link #dispatchToFacility}); it is also exposed via a REST endpoint for
 * testing the plumbing.
 */
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {
    private final UserFacilityRepository userFacilityRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository recipientRepository;
    private final NotificationBroadcaster broadcaster;

    /** Notify every active user assigned to a facility. */
    public Mono<NotificationDTO.NotificationResponse> dispatchToFacility(Long facilityId, NotificationDTO.DispatchRequest request) {
        return dispatchToFacility(facilityId, request, null);
    }

    /** Notify a facility's active users, optionally excluding one (e.g. the clinician who acted). */
    public Mono<NotificationDTO.NotificationResponse> dispatchToFacility(
            Long facilityId, NotificationDTO.DispatchRequest request, UUID excludeUserId) {
        return userFacilityRepository.findActiveUserIdsByFacilityId(facilityId)
                .filter(userId -> excludeUserId == null || !userId.equals(excludeUserId))
                .collectList()
                .flatMap(userIds -> persistAndBroadcast(facilityId, userIds, request));
    }

    /** Notify a single user directly. */
    public Mono<NotificationDTO.NotificationResponse> dispatchToUser(UUID userId, NotificationDTO.DispatchRequest request) {
        return persistAndBroadcast(request.getFacilityId(), List.of(userId), request);
    }

    private Mono<NotificationDTO.NotificationResponse> persistAndBroadcast(
            Long facilityId, List<UUID> userIds, NotificationDTO.DispatchRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        Notification notification = Notification.builder()
                .type(request.getType())
                .title(request.getTitle())
                .body(request.getBody())
                .data(request.getData())
                .facilityId(facilityId)
                .correlationId(request.getCorrelationId())
                .createdAt(now)
                .build();

        return notificationRepository.save(notification).flatMap(saved -> {
            NotificationDTO.NotificationResponse response = toResponse(saved);
            List<NotificationRecipient> recipients = userIds.stream()
                    .map(userId -> NotificationRecipient.builder()
                            .notificationId(saved.getId())
                            .userId(userId)
                            .createdAt(now)
                            .build())
                    .toList();

            return recipientRepository.saveAll(recipients)
                    .then(broadcaster.broadcast(userIds, response))
                    .thenReturn(response);
        });
    }

    private NotificationDTO.NotificationResponse toResponse(Notification n) {
        return NotificationDTO.NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .body(n.getBody())
                .data(n.getData())
                .facilityId(n.getFacilityId())
                .correlationId(n.getCorrelationId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
