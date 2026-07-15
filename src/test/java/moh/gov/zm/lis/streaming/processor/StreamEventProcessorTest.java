package moh.gov.zm.lis.streaming.processor;

import moh.gov.zm.lis.messaging.dto.InboundEventLogDTO;
import moh.gov.zm.lis.messaging.service.InboundEventService;
import moh.gov.zm.lis.redis.idempotency.MessageIdempotencyCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StreamEventProcessorTest {

    private static final String ID = "MSG-1";

    @Mock
    private MessageIdempotencyCache idempotencyCache;
    @Mock
    private InboundEventService inboundEventService;

    private final InboundEventLogDTO.InboundEventLogResponse response =
            mock(InboundEventLogDTO.InboundEventLogResponse.class);

    private TestProcessor processor;

    /** Concrete processor that records dispatch / failure-hook invocations. */
    private static class TestProcessor extends StreamEventProcessor {
        final AtomicInteger dispatchCount = new AtomicInteger();
        final AtomicInteger failureHookCount = new AtomicInteger();
        final AtomicReference<Throwable> lastFailure = new AtomicReference<>();
        boolean failDispatch = false;

        TestProcessor(MessageIdempotencyCache c, InboundEventService s) {
            super(c, s);
        }

        @Override
        public String topicName() {
            return "test-topic";
        }

        @Override
        protected EventDescriptor describe(String rawValue) {
            return new EventDescriptor(ID, "TEST_EVENT", "test", null);
        }

        @Override
        protected Mono<Void> dispatch(EventDescriptor descriptor, String rawValue) {
            dispatchCount.incrementAndGet();
            return failDispatch ? Mono.error(new RuntimeException("boom")) : Mono.empty();
        }

        @Override
        protected Mono<Void> onProcessingFailure(EventDescriptor descriptor, String rawValue, Throwable error) {
            failureHookCount.incrementAndGet();
            lastFailure.set(error);
            return Mono.empty();
        }
    }

    @BeforeEach
    void setUp() {
        processor = new TestProcessor(idempotencyCache, inboundEventService);
        when(inboundEventService.recordEvent(any())).thenReturn(Mono.just(response));
        when(inboundEventService.markProcessed(ID)).thenReturn(Mono.just(response));
        when(inboundEventService.markFailed(eq(ID), any())).thenReturn(Mono.just(response));
        when(idempotencyCache.markProcessed(ID)).thenReturn(Mono.empty());
        when(idempotencyCache.evict(ID)).thenReturn(Mono.empty());
    }

    @Test
    void processesFreshMessageAndMarksItDone() {
        when(idempotencyCache.isProcessed(ID)).thenReturn(Mono.just(false));
        when(inboundEventService.isAlreadyProcessed(ID)).thenReturn(Mono.just(false));

        processor.handle("k", "raw");

        assertThat(processor.dispatchCount).hasValue(1);
        verify(inboundEventService).recordEvent(any());
        verify(inboundEventService).markProcessed(ID);
        verify(idempotencyCache).markProcessed(ID);
        assertThat(processor.failureHookCount).hasValue(0);
    }

    @Test
    void skipsWhenRedisReportsAlreadyProcessed() {
        when(idempotencyCache.isProcessed(ID)).thenReturn(Mono.just(true));

        processor.handle("k", "raw");

        assertThat(processor.dispatchCount).hasValue(0);
        verify(inboundEventService, never()).isAlreadyProcessed(any());
        verify(inboundEventService, never()).recordEvent(any());
    }

    @Test
    void skipsAndBackfillsCacheWhenDbReportsAlreadyProcessed() {
        when(idempotencyCache.isProcessed(ID)).thenReturn(Mono.just(false));
        when(inboundEventService.isAlreadyProcessed(ID)).thenReturn(Mono.just(true));

        processor.handle("k", "raw");

        assertThat(processor.dispatchCount).hasValue(0);
        verify(inboundEventService, never()).recordEvent(any());
        verify(idempotencyCache).markProcessed(ID); // backfill so the next Redis check short-circuits
    }

    @Test
    void onDispatchFailureMarksFailedEvictsAndInvokesHook() {
        processor.failDispatch = true;
        when(idempotencyCache.isProcessed(ID)).thenReturn(Mono.just(false));
        when(inboundEventService.isAlreadyProcessed(ID)).thenReturn(Mono.just(false));

        processor.handle("k", "raw");

        // markFailed + evict + the failure hook define the error path. (markProcessed is
        // assembled as a `.then(...)` argument but never subscribed on error, so it is not
        // verifiable via never() here.)
        verify(inboundEventService).markFailed(eq(ID), any());
        verify(idempotencyCache).evict(ID);
        assertThat(processor.failureHookCount).hasValue(1);
        assertThat(processor.lastFailure.get()).isInstanceOf(RuntimeException.class).hasMessage("boom");
    }

    @Test
    void blankValueIsIgnored() {
        processor.handle("k", "   ");

        assertThat(processor.dispatchCount).hasValue(0);
        verify(idempotencyCache, never()).isProcessed(any());
    }
}
