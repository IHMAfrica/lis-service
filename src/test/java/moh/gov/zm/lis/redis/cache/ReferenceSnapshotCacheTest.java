package moh.gov.zm.lis.redis.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferenceSnapshotCacheTest {

    private static final String KEY = "lis:ref:snapshot:widget";

    @Mock
    private ReactiveStringRedisTemplate redisTemplate;
    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public static class Widget {
        public String code;
        public int id;

        public Widget() {
        }

        public Widget(String code, int id) {
            this.code = code;
            this.id = id;
        }
    }

    private ReferenceSnapshotCache cache() {
        return new ReferenceSnapshotCache(redisTemplate, objectMapper);
    }

    @Test
    void returnsCachedSnapshotWithoutCallingLoader() throws Exception {
        String json = objectMapper.writeValueAsString(List.of(new Widget("A", 1)));
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(KEY)).thenReturn(Mono.just(json));
        AtomicInteger loaderCalls = new AtomicInteger();

        StepVerifier.create(cache().snapshot("widget", Widget.class, () -> {
                    loaderCalls.incrementAndGet();
                    return Mono.just(List.of());
                }))
                .assertNext(list -> {
                    org.assertj.core.api.Assertions.assertThat(list).hasSize(1);
                    org.assertj.core.api.Assertions.assertThat(list.getFirst().code).isEqualTo("A");
                })
                .verifyComplete();
        org.assertj.core.api.Assertions.assertThat(loaderCalls).hasValue(0);
    }

    @Test
    void loadsAndCachesOnMiss() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(KEY)).thenReturn(Mono.empty());
        when(valueOps.set(eq(KEY), anyString(), any(Duration.class))).thenReturn(Mono.just(true));

        StepVerifier.create(cache().snapshot("widget", Widget.class, () -> Mono.just(List.of(new Widget("B", 2)))))
                .assertNext(list -> org.assertj.core.api.Assertions.assertThat(list.getFirst().code).isEqualTo("B"))
                .verifyComplete();
        verify(valueOps).set(eq(KEY), anyString(), any(Duration.class));
    }

    @Test
    void fallsBackToLoaderOnRedisError() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(KEY)).thenReturn(Mono.error(new RuntimeException("redis down")));

        StepVerifier.create(cache().snapshot("widget", Widget.class, () -> Mono.just(List.of(new Widget("C", 3)))))
                .assertNext(list -> org.assertj.core.api.Assertions.assertThat(list.getFirst().code).isEqualTo("C"))
                .verifyComplete();
    }

    @Test
    void evictDeletesKey() {
        when(redisTemplate.delete(KEY)).thenReturn(Mono.just(1L));

        StepVerifier.create(cache().evict("widget")).verifyComplete();
    }
}
