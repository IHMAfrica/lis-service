package moh.gov.zm.lis.messaging.publisher;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LisTopicRouterTest {

    private final LisTopicRouter router = new LisTopicRouter("lis.dlq", "lab-orders", "lab-result-acks");

    @Test
    void resolvesLabOrderCreatedToLabOrdersTopic() {
        assertThat(router.resolve("LAB_ORDER_CREATED")).isEqualTo("lab-orders");
    }

    @Test
    void resolvesLabResultAckCreatedToLabResultAcksTopic() {
        assertThat(router.resolve("LAB_RESULT_ACK_CREATED")).isEqualTo("lab-result-acks");
    }

    @Test
    void unknownEventTypeFallsBackToDeadLetter() {
        assertThat(router.resolve("SOMETHING_ELSE")).isEqualTo("lis.dlq");
        assertThat(router.resolve(null)).isEqualTo("lis.dlq");
    }
}
