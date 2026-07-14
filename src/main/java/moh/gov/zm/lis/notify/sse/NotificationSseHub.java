package moh.gov.zm.lis.notify.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.notify.dto.NotificationDTO;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-instance hub of live SSE connections. Subscribes once to the Redis Pub/Sub
 * channel; when a broadcast arrives it forwards the notification to any of the
 * targeted users that are connected to <em>this</em> instance. This is what lets
 * SSE scale horizontally: a notification created on any instance reaches the user
 * regardless of which instance holds their connection.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSseHub {
    private static final Duration HEARTBEAT = Duration.ofSeconds(20);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private final Map<UUID, Sinks.Many<ServerSentEvent<Object>>> sinks = new ConcurrentHashMap<>();
    private Disposable subscription;

    @PostConstruct
    void subscribe() {
        subscription = redisTemplate.listenToChannel(NotificationBroadcaster.CHANNEL)
                .map(ReactiveSubscription.Message::getMessage)
                .doOnNext(this::route)
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(30)))
                .subscribe();
        log.info("Subscribed to Redis notification channel '{}'", NotificationBroadcaster.CHANNEL);
    }

    @PreDestroy
    void shutdown() {
        if (subscription != null) {
            subscription.dispose();
        }
    }

    /** Live SSE stream of notifications for a user, plus periodic keep-alive comments. */
    public Flux<ServerSentEvent<Object>> stream(UUID userId) {
        Sinks.Many<ServerSentEvent<Object>> sink =
                sinks.computeIfAbsent(userId, k -> Sinks.many().multicast().onBackpressureBuffer());

        Flux<ServerSentEvent<Object>> heartbeat = Flux.interval(HEARTBEAT, HEARTBEAT)
                .map(i -> ServerSentEvent.<Object>builder().comment("keep-alive").build());

        return sink.asFlux()
                .mergeWith(heartbeat)
                .doFinally(signal -> {
                    if (sink.currentSubscriberCount() == 0) {
                        sinks.remove(userId, sink);
                    }
                });
    }

    private void route(String json) {
        try {
            NotificationBroadcast broadcast = objectMapper.readValue(json, NotificationBroadcast.class);
            for (UUID userId : broadcast.getUserIds()) {
                emit(userId, broadcast.getNotification());
            }
        } catch (Exception e) {
            log.warn("Dropping malformed notification broadcast: {}", e.getMessage());
        }
    }

    private void emit(UUID userId, NotificationDTO.NotificationResponse notification) {
        Sinks.Many<ServerSentEvent<Object>> sink = sinks.get(userId);
        if (sink == null) {
            return; // this user has no connection on this instance
        }
        sink.tryEmitNext(ServerSentEvent.<Object>builder(notification)
                .id(notification.getId().toString())
                .event("notification")
                .build());
    }
}
