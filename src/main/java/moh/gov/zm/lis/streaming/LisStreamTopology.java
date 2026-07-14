package moh.gov.zm.lis.streaming;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moh.gov.zm.lis.streaming.processor.StreamEventProcessor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builds the Kafka Streams topology by registering every {@link StreamEventProcessor}
 * against the topic it consumes. New inbound streams are added simply by declaring
 * another handler bean.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LisStreamTopology {
    private final StreamsBuilder streamsBuilder;
    private final List<StreamEventProcessor> processors;

    @PostConstruct
    public void buildTopology() {
        if (processors.isEmpty()) {
            log.warn("No stream event processors registered — no Kafka Streams topology built");
            return;
        }
        processors.forEach(processor -> {
            streamsBuilder.stream(processor.topicName(), Consumed.with(Serdes.String(), Serdes.String()))
                    .foreach(processor::handle);
            log.info("Registered Kafka stream for topic '{}'", processor.topicName());
        });
    }
}
