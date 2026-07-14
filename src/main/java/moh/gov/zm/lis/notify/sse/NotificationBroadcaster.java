package moh.gov.zm.lis.notify.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.notify.dto.NotificationDTO;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Publishes a notification to the Redis Pub/Sub channel so every application
 * instance can deliver it to its locally-connected SSE clients. Redis being
 * unavailable degrades live delivery only — the notification is already
 * persisted, so recipients still see it on their next fetch.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationBroadcaster {
    /** Channel every instance subscribes to; see {@link NotificationSseHub}. */
    public static final String CHANNEL = "lis:notify:stream";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Mono<Void> broadcast(List<UUID> userIds, NotificationDTO.NotificationResponse notification) {
        if (userIds.isEmpty()) {
            return Mono.empty();
        }
        try {
            String json = objectMapper.writeValueAsString(new NotificationBroadcast(userIds, notification));
            return redisTemplate.convertAndSend(CHANNEL, json)
                    .doOnError(ex -> log.warn("Failed to publish notification to Redis: {}", ex.getMessage()))
                    .onErrorResume(ex -> Mono.empty())
                    .then();
        } catch (Exception e) {
            log.warn("Failed to serialize notification broadcast: {}", e.getMessage());
            return Mono.empty();
        }
    }
}
