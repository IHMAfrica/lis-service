package moh.gov.zm.lis.messaging.service;

import moh.gov.zm.lis.messaging.dto.OutboundEventOutboxDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OutboundEventService {
    Mono<OutboundEventOutboxDTO.OutboundEventOutboxResponse> enqueueEvent(OutboundEventOutboxDTO.CreateOutboundEventOutboxRequest request);

    Mono<OutboundEventOutboxDTO.OutboundEventOutboxResponse> findById(UUID id);

    Flux<OutboundEventOutboxDTO.OutboundEventOutboxResponse> fetchPendingBatch(Integer batchSize);

    Mono<OutboundEventOutboxDTO.OutboundEventOutboxResponse> markPublished(UUID id);

    Mono<OutboundEventOutboxDTO.OutboundEventOutboxResponse> markPublishFailed(UUID id, String errorMessage);

    Mono<Long> countPending();
}
