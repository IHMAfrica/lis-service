package moh.gov.zm.lis.notify.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import moh.gov.zm.lis.notify.dto.NotificationDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationBroadcasterTest {

    private static final UUID USER = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private NotificationBroadcaster broadcaster() {
        return new NotificationBroadcaster(redisTemplate, objectMapper);
    }

    private NotificationDTO.NotificationResponse notification() {
        return NotificationDTO.NotificationResponse.builder()
                .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .type("LAB_RESULT").title("Lab result received").build();
    }

    @Test
    void broadcastPublishesJsonToTheRedisChannel() {
        when(redisTemplate.convertAndSend(eq(NotificationBroadcaster.CHANNEL), anyString())).thenReturn(Mono.just(1L));

        StepVerifier.create(broadcaster().broadcast(List.of(USER), notification())).verifyComplete();

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).convertAndSend(eq(NotificationBroadcaster.CHANNEL), json.capture());
        assertThat(json.getValue())
                .contains(USER.toString())
                .contains("11111111-1111-1111-1111-111111111111");
    }

    @Test
    void emptyRecipientListDoesNotPublish() {
        StepVerifier.create(broadcaster().broadcast(List.of(), notification())).verifyComplete();

        verify(redisTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void redisErrorIsSwallowedSoDeliveryDegradesGracefully() {
        when(redisTemplate.convertAndSend(eq(NotificationBroadcaster.CHANNEL), anyString()))
                .thenReturn(Mono.error(new RuntimeException("redis down")));

        // Notification is already persisted; a Pub/Sub failure must not surface as an error.
        StepVerifier.create(broadcaster().broadcast(List.of(USER), notification())).verifyComplete();
    }
}
