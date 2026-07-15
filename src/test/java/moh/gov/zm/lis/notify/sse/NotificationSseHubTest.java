package moh.gov.zm.lis.notify.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import moh.gov.zm.lis.notify.dto.NotificationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSseHubTest {

    private static final UUID CONNECTED = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Sinks.Many<ReactiveSubscription.Message<String, String>> channel;
    private NotificationSseHub hub;

    @BeforeEach
    void setUp() {
        channel = Sinks.many().multicast().onBackpressureBuffer();
        // doReturn avoids the wildcard-capture mismatch on listenToChannel's return type.
        doReturn(channel.asFlux()).when(redisTemplate).listenToChannel(NotificationBroadcaster.CHANNEL);
        hub = new NotificationSseHub(redisTemplate, objectMapper);
        hub.subscribe(); // @PostConstruct: begin listening to the Redis channel
    }

    private NotificationDTO.NotificationResponse notification() {
        return NotificationDTO.NotificationResponse.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .type("LAB_RESULT").title("Lab result received").build();
    }

    private String broadcastJson(List<UUID> users) throws Exception {
        return objectMapper.writeValueAsString(new NotificationBroadcast(users, notification()));
    }

    @SuppressWarnings("unchecked")
    private ReactiveSubscription.Message<String, String> message(String payload) {
        ReactiveSubscription.Message<String, String> m = mock(ReactiveSubscription.Message.class);
        when(m.getMessage()).thenReturn(payload);
        return m;
    }

    @Test
    void deliversNotificationToAConnectedTargetUser() throws Exception {
        String json = broadcastJson(List.of(CONNECTED));
        Flux<ServerSentEvent<Object>> stream = hub.stream(CONNECTED); // registers this user's sink

        StepVerifier.create(stream.next())
                .then(() -> channel.tryEmitNext(message(json)))
                .assertNext(sse -> {
                    assertThat(sse.event()).isEqualTo("notification");
                    assertThat(sse.data()).isInstanceOf(NotificationDTO.NotificationResponse.class);
                    assertThat(((NotificationDTO.NotificationResponse) sse.data()).getType()).isEqualTo("LAB_RESULT");
                })
                .verifyComplete();
    }

    @Test
    void doesNotDeliverNotificationTargetedAtAnotherUser() throws Exception {
        String json = broadcastJson(List.of(OTHER)); // broadcast for a user not connected here

        StepVerifier.create(hub.stream(CONNECTED))
                .then(() -> channel.tryEmitNext(message(json)))
                .expectNoEvent(Duration.ofMillis(300))
                .thenCancel()
                .verify();
    }

    @Test
    void malformedBroadcastIsDroppedAndStreamKeepsWorking() throws Exception {
        String good = broadcastJson(List.of(CONNECTED));
        Flux<ServerSentEvent<Object>> stream = hub.stream(CONNECTED);

        StepVerifier.create(stream.next())
                .then(() -> channel.tryEmitNext(message("this-is-not-json"))) // dropped, must not break the stream
                .then(() -> channel.tryEmitNext(message(good)))               // still delivered
                .assertNext(sse -> assertThat(sse.event()).isEqualTo("notification"))
                .verifyComplete();
    }
}
