package moh.gov.zm.lis.notify.service;

import moh.gov.zm.lis.iam.repository.UserFacilityRepository;
import moh.gov.zm.lis.notify.dto.NotificationDTO;
import moh.gov.zm.lis.notify.entity.Notification;
import moh.gov.zm.lis.notify.entity.NotificationRecipient;
import moh.gov.zm.lis.notify.repository.NotificationRecipientRepository;
import moh.gov.zm.lis.notify.repository.NotificationRepository;
import moh.gov.zm.lis.notify.sse.NotificationBroadcaster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationDispatcherTest {

    private static final Long FACILITY = 7L;
    private static final UUID U1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID U2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID U3 = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Mock private UserFacilityRepository userFacilityRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationRecipientRepository recipientRepository;
    @Mock private NotificationBroadcaster broadcaster;

    private NotificationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new NotificationDispatcher(userFacilityRepository, notificationRepository,
                recipientRepository, broadcaster);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            if (n.getId() == null) {
                n.setId(UUID.randomUUID());
            }
            return Mono.just(n);
        });
        when(recipientRepository.saveAll(anyList())).thenReturn(Flux.empty());
        when(broadcaster.broadcast(anyList(), any())).thenReturn(Mono.empty());
    }

    private NotificationDTO.DispatchRequest request() {
        return NotificationDTO.DispatchRequest.builder()
                .type("LAB_RESULT").title("Lab result received").body("A result arrived")
                .facilityId(FACILITY).correlationId(UUID.randomUUID()).build();
    }

    @SuppressWarnings("unchecked")
    private List<NotificationRecipient> capturedRecipients() {
        ArgumentCaptor<List<NotificationRecipient>> captor = ArgumentCaptor.forClass(List.class);
        verify(recipientRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    @Test
    void dispatchToFacilityPersistsRecipientsForAllActiveUsersAndBroadcasts() {
        when(userFacilityRepository.findActiveUserIdsByFacilityId(FACILITY)).thenReturn(Flux.just(U1, U2));

        StepVerifier.create(dispatcher.dispatchToFacility(FACILITY, request()))
                .assertNext(resp -> {
                    assertThat(resp.getId()).isNotNull();
                    assertThat(resp.getType()).isEqualTo("LAB_RESULT");
                    assertThat(resp.getFacilityId()).isEqualTo(FACILITY);
                })
                .verifyComplete();

        verify(notificationRepository).save(any(Notification.class));
        assertThat(capturedRecipients()).extracting(NotificationRecipient::getUserId)
                .containsExactlyInAnyOrder(U1, U2);
        verify(broadcaster).broadcast(eq(List.of(U1, U2)), any());
    }

    @Test
    void dispatchToFacilityExcludesTheActingUser() {
        when(userFacilityRepository.findActiveUserIdsByFacilityId(FACILITY)).thenReturn(Flux.just(U1, U2, U3));

        StepVerifier.create(dispatcher.dispatchToFacility(FACILITY, request(), U2)).expectNextCount(1).verifyComplete();

        assertThat(capturedRecipients()).extracting(NotificationRecipient::getUserId)
                .containsExactlyInAnyOrder(U1, U3)
                .doesNotContain(U2);
        verify(broadcaster).broadcast(eq(List.of(U1, U3)), any());
    }

    @Test
    void dispatchToUserNotifiesOnlyThatUser() {
        StepVerifier.create(dispatcher.dispatchToUser(U1, request())).expectNextCount(1).verifyComplete();

        verify(userFacilityRepository, never()).findActiveUserIdsByFacilityId(any());
        assertThat(capturedRecipients()).extracting(NotificationRecipient::getUserId).containsExactly(U1);
        verify(broadcaster).broadcast(eq(List.of(U1)), any());
    }

    @Test
    void dispatchToFacilityWithNoActiveUsersStillPersistsNotification() {
        when(userFacilityRepository.findActiveUserIdsByFacilityId(FACILITY)).thenReturn(Flux.empty());

        StepVerifier.create(dispatcher.dispatchToFacility(FACILITY, request())).expectNextCount(1).verifyComplete();

        // notification is still recorded; no recipients, broadcast invoked with an empty list
        verify(notificationRepository).save(any(Notification.class));
        assertThat(capturedRecipients()).isEmpty();
        verify(broadcaster).broadcast(eq(List.of()), any());
    }
}
