package moh.gov.zm.lis.redis.cache;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * Caches whole-table snapshots of rarely-changing reference / catalogue data
 * (province, district, facility, lab_type, test, laboratory). Each table's full
 * row set is stored under a single Redis key as a JSON list; callers then filter,
 * paginate or index it in memory. This keeps these hot-but-static reads off the
 * database entirely.
 *
 * <p>On any Redis error the loader is used directly, so the cache is never a hard
 * dependency. Mutations to a cached table must call {@link #evict(String)} so the
 * next read repopulates from the database.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class
ReferenceSnapshotCache {
    private static final String KEY_PREFIX = "lis:ref:snapshot:";
    private static final Duration TTL = Duration.ofHours(24);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Return the cached snapshot of {@code table}, loading and caching it on a miss.
     * Falls back to {@code loader} on any Redis or deserialization error.
     */
    public <T> Mono<List<T>> snapshot(String table, Class<T> type, Supplier<Mono<List<T>>> loader) {
        String key = KEY_PREFIX + table;
        return redisTemplate.opsForValue().get(key)
                .flatMap(json -> deserialize(json, type))
                .switchIfEmpty(Mono.defer(() -> loadAndCache(key, loader)))
                .onErrorResume(ex -> {
                    log.warn("Redis snapshot error for table='{}': {} — loading from DB", table, ex.getMessage());
                    return loader.get();
                });
    }

    public Mono<Void> evict(String table) {
        return redisTemplate.delete(KEY_PREFIX + table)
                .doOnNext(n -> log.debug("Evicted reference snapshot for table='{}'", table))
                .onErrorResume(ex -> {
                    log.warn("Failed to evict reference snapshot for table='{}': {}", table, ex.getMessage());
                    return Mono.just(0L);
                })
                .then();
    }

    private <T> Mono<List<T>> loadAndCache(String key, Supplier<Mono<List<T>>> loader) {
        return loader.get().flatMap(list -> {
            try {
                String json = objectMapper.writeValueAsString(list);
                return redisTemplate.opsForValue().set(key, json, TTL)
                        .thenReturn(list)
                        .onErrorReturn(list);
            } catch (Exception e) {
                return Mono.just(list);
            }
        });
    }

    private <T> Mono<List<T>> deserialize(String json, Class<T> type) {
        try {
            JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, type);
            return Mono.just(objectMapper.readValue(json, listType));
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
