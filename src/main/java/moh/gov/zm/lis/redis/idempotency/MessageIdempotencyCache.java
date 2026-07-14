package moh.gov.zm.lis.redis.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageIdempotencyCache {
    private static final String KEY_PREFIX = "lis:idempotency:msg:";

    private final ReactiveStringRedisTemplate redisTemplate;

    public Mono<Boolean> isProcessed(String messageId) {
        return redisTemplate.hasKey(KEY_PREFIX + messageId)
                .onErrorResume(ex -> {
                    log.warn("Redis idempotency check failed for messageId='{}' — falling through to DB: {}",
                            messageId, ex.getMessage());
                    return Mono.just(false);
                });
    }

    public Mono<Void> markProcessed(String messageId) {
        Duration ttl = Duration.ofHours(24);

        return redisTemplate.opsForValue()
                .set(KEY_PREFIX + messageId, "1", ttl)
                .doOnSuccess(_ -> log.debug("Cached idempotency key for messageId='{}'", messageId))
                .onErrorResume(ex -> {
                    log.warn("Failed to cache idempotency key for messageId='{}': {}", messageId, ex.getMessage());

                    return Mono.just(false);
                })
                .then();
    }

    public Mono<Void> evict(String messageId) {
        return redisTemplate.delete(KEY_PREFIX + messageId).then();
    }
}
