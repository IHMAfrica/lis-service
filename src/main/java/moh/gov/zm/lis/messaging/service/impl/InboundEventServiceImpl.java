package moh.gov.zm.lis.messaging.service.impl;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.messaging.dto.InboundEventLogDTO;
import moh.gov.zm.lis.messaging.entity.InboundEventLog;
import moh.gov.zm.lis.messaging.repository.InboundEventLogRepository;
import moh.gov.zm.lis.messaging.service.InboundEventService;
import moh.gov.zm.lis.redis.cache.ReferenceDataCache;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InboundEventServiceImpl implements InboundEventService {
    private final InboundEventLogRepository inboundEventLogRepository;
    private final ReferenceDataCache refCache;

    @Override
    public Mono<Boolean> isAlreadyProcessed(String messageId) {
        return inboundEventLogRepository.existsByMessageId(messageId);
    }

    @Override
    public Mono<InboundEventLogDTO.InboundEventLogResponse> recordEvent(InboundEventLogDTO.RecordInboundEventRequest request) {
        // An unknown event-type code must not lose the log entry: event_type_id is
        // nullable, and the log's primary job is idempotency / audit of what arrived.
        return refCache.getInboundEventTypeId(request.getEventTypeCode())
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(eventTypeId -> inboundEventLogRepository.save(InboundEventLog.builder()
                        .messageId(request.getMessageId())
                        .topic(request.getTopic())
                        .eventTypeId(eventTypeId.orElse(null))
                        .eventTypeCode(request.getEventTypeCode())
                        .sourceService(request.getSourceService())
                        .correlationId(request.getCorrelationId())
                        .processingStatus("PROCESSING")
                        .receivedAt(OffsetDateTime.now())
                        .payload(request.getPayload())
                        .build()))
                .map(this::toResponse);
    }

    @Override
    public Mono<InboundEventLogDTO.InboundEventLogResponse> markProcessed(String messageId) {
        return inboundEventLogRepository.findByMessageId(messageId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("InboundEventLog not found: " + messageId)))
                .flatMap(log -> {
                    log.setProcessingStatus("PROCESSED");
                    log.setProcessedAt(OffsetDateTime.now());
                    log.setErrorMessage(null);

                    return inboundEventLogRepository.save(log);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<InboundEventLogDTO.InboundEventLogResponse> markFailed(String messageId, String errorMessage) {
        return inboundEventLogRepository.findByMessageId(messageId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("InboundEventLog not found: " + messageId)))
                .flatMap(log -> {
                    log.setProcessingStatus("FAILED");
                    log.setErrorMessage(errorMessage);
                    log.setProcessedAt(OffsetDateTime.now());

                    return inboundEventLogRepository.save(log);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<InboundEventLogDTO.InboundEventLogResponse> findByMessageId(String messageId) {
        return inboundEventLogRepository.findByMessageId(messageId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("InboundEventLog not found: " + messageId)))
                .map(this::toResponse);
    }

    @Override
    public Flux<InboundEventLogDTO.InboundEventLogResponse> findAllByStatus(String processingStatus) {
        return inboundEventLogRepository.findAllByProcessingStatus(processingStatus)
                .map(this::toResponse);
    }

    private InboundEventLogDTO.InboundEventLogResponse toResponse(InboundEventLog e) {
        return InboundEventLogDTO.InboundEventLogResponse.builder()
                .id(e.getId())
                .messageId(e.getMessageId())
                .topic(e.getTopic())
                .eventTypeId(e.getEventTypeId())
                .eventTypeCode(e.getEventTypeCode())
                .sourceService(e.getSourceService())
                .correlationId(e.getCorrelationId())
                .processingStatus(e.getProcessingStatus())
                .errorMessage(e.getErrorMessage())
                .receivedAt(e.getReceivedAt())
                .processedAt(e.getProcessedAt())
                .build();
    }
}
