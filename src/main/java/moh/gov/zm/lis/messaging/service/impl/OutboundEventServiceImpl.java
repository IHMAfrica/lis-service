package moh.gov.zm.lis.messaging.service.impl;

import lombok.RequiredArgsConstructor;
import moh.gov.zm.lis.messaging.dto.OutboundEventOutboxDTO;
import moh.gov.zm.lis.messaging.entity.OutboundEventOutbox;
import moh.gov.zm.lis.messaging.repository.OutboundEventOutboxRepository;
import moh.gov.zm.lis.messaging.service.OutboundEventService;
import moh.gov.zm.lis.ref.repository.OutboundEventTypeRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboundEventServiceImpl implements OutboundEventService {
    private final OutboundEventOutboxRepository outboundEventOutboxRepository;
    private final OutboundEventTypeRepository outboundEventTypeRepository;

    @Override
    public Mono<OutboundEventOutboxDTO.OutboundEventOutboxResponse> enqueueEvent(OutboundEventOutboxDTO.CreateOutboundEventOutboxRequest request) {
        return outboundEventOutboxRepository.save(OutboundEventOutbox.builder()
                        .eventTypeId(request.getEventTypeId())
                        .payload(request.getPayload())
                        .topic(request.getTopic())
                        .correlationId(request.getCorrelationId())
                        .createdBy(request.getCreatedBy())
                        .isPublished(false)
                        .retryCount((short) 0)
                        .createdAt(OffsetDateTime.now())
                        .build())
                .flatMap(this::toResponse);
    }

    @Override
    public Mono<OutboundEventOutboxDTO.OutboundEventOutboxResponse> findById(UUID id) {
        return outboundEventOutboxRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("OutboundEventOutbox not found: " + id)))
                .flatMap(this::toResponse);
    }

    @Override
    public Flux<OutboundEventOutboxDTO.OutboundEventOutboxResponse> fetchPendingBatch(Integer batchSize) {
        return outboundEventOutboxRepository.findAllByIsPublishedFalseOrderByCreatedAtAsc()
                .take(batchSize)
                .flatMap(this::toResponse);
    }

    @Override
    public Mono<OutboundEventOutboxDTO.OutboundEventOutboxResponse> markPublished(UUID id) {
        return outboundEventOutboxRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("OutboundEventOutbox not found: " + id)))
                .flatMap(row -> {
                    row.setIsPublished(true);
                    row.setPublishedAt(OffsetDateTime.now());

                    return outboundEventOutboxRepository.save(row);
                })
                .flatMap(this::toResponse);
    }

    @Override
    public Mono<OutboundEventOutboxDTO.OutboundEventOutboxResponse> markPublishFailed(UUID id, String errorMessage) {
        return outboundEventOutboxRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("OutboundEventOutbox not found: " + id)))
                .flatMap(row -> {
                    row.setRetryCount((short) (row.getRetryCount() + 1));
                    row.setLastError(errorMessage);

                    return outboundEventOutboxRepository.save(row);
                })
                .flatMap(this::toResponse);
    }

    @Override
    public Mono<Long> countPending() {
        return outboundEventOutboxRepository.countByIsPublishedFalse();
    }

    private Mono<OutboundEventOutboxDTO.OutboundEventOutboxResponse> toResponse(OutboundEventOutbox e) {
        return outboundEventTypeRepository.findById(e.getEventTypeId())
                .map(et -> OutboundEventOutboxDTO.OutboundEventOutboxResponse.builder()
                        .id(e.getId())
                        .eventTypeId(e.getEventTypeId())
                        .topic(e.getTopic())
                        .eventTypeCode(et.getCode())
                        .eventTypeName(et.getName())
                        .correlationId(e.getCorrelationId())
                        .isPublished(e.getIsPublished())
                        .publishedAt(e.getPublishedAt())
                        .retryCount(e.getRetryCount())
                        .lastError(e.getLastError())
                        .createdAt(e.getCreatedAt())
                        .build()
                )
                .switchIfEmpty(
                        Mono.just(
                                OutboundEventOutboxDTO.OutboundEventOutboxResponse.builder()
                                        .id(e.getId())
                                        .eventTypeId(e.getEventTypeId())
                                        .eventTypeCode(null)
                                        .eventTypeName(null)
                                        .topic(e.getTopic())
                                        .correlationId(e.getCorrelationId())
                                        .isPublished(e.getIsPublished())
                                        .publishedAt(e.getPublishedAt())
                                        .retryCount(e.getRetryCount())
                                        .lastError(e.getLastError())
                                        .createdAt(e.getCreatedAt())
                                        .build()
                        )
                );
    }
}
