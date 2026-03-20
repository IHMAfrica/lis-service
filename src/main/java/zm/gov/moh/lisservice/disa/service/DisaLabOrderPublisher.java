package zm.gov.moh.lisservice.disa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import zm.gov.moh.lisservice.config.ConfigProperties;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisaLabOrderPublisher {

    private final KafkaSender<String, String> kafkaSender;
    private final ConfigProperties configProperties;

    public Mono<Void> publish(String hl7Message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(
                configProperties.getLabOrderTopic(), hl7Message);
        SenderRecord<String, String, String> senderRecord = SenderRecord.create(record, hl7Message);

        return kafkaSender.send(Mono.just(senderRecord))
                .flatMap(result -> {
                    if (result.exception() != null) {
                        log.error("Failed to deliver HL7 lab order to Kafka topic {}: {}",
                                configProperties.getLabOrderTopic(), result.exception().getMessage());
                        return Mono.error(result.exception());
                    }
                    log.debug("Published HL7 lab order to topic {} partition {} offset {}",
                            configProperties.getLabOrderTopic(),
                            result.recordMetadata().partition(),
                            result.recordMetadata().offset());
                    return Mono.empty();
                })
                .doOnError(e -> log.error("Kafka send error for lab order", e))
                .then();
    }
}
