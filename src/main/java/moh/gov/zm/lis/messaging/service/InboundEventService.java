package moh.gov.zm.lis.messaging.service;

import moh.gov.zm.lis.messaging.dto.InboundEventLogDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InboundEventService {
    Mono<Boolean> isAlreadyProcessed(String messageId);

    Mono<InboundEventLogDTO.InboundEventLogResponse> recordEvent(InboundEventLogDTO.RecordInboundEventRequest request);

    Mono<InboundEventLogDTO.InboundEventLogResponse> markProcessed(String messageId);

    Mono<InboundEventLogDTO.InboundEventLogResponse> markFailed(String messageId, String errorMessage);

    Mono<InboundEventLogDTO.InboundEventLogResponse> findByMessageId(String messageId);

    Flux<InboundEventLogDTO.InboundEventLogResponse> findAllByStatus(String processingStatus);
}
