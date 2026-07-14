package moh.gov.zm.lis.messaging.publisher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps an outbound event type code to the Kafka topic it is relayed to. Unknown
 * codes fall through to the dead-letter topic so nothing is silently dropped.
 */
@Component
public class LisTopicRouter {
    private final String dlq;
    private final Map<String, String> routes = new HashMap<>();

    public LisTopicRouter(
            @Value("${spring.kafka.topic.dlq:lis.dlq}") String dlq,
            @Value("${spring.kafka.topic.lab-orders:lab-orders}") String labOrdersTopic,
            @Value("${spring.kafka.topic.lab-result-acks:lab-result-ack}") String labResultAcksTopic
    ) {
        this.dlq = dlq;

        routes.put("LAB_ORDER_CREATED", labOrdersTopic);
        routes.put("LAB_RESULT_ACK_CREATED", labResultAcksTopic);
    }

    public String resolve(String eventTypeCode) {
        return routes.getOrDefault(eventTypeCode, dlq);
    }
}
