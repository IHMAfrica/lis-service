package moh.gov.zm.lis.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ConfigProperties {
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String kafkaBootstrapServers;

    @Value("${spring.kafka.streams.application-id:${spring.application.name:lis}}")
    private String kafkaStreamsApplicationId;

    @Value("${spring.kafka.topic.lab-orders-ack:lab-orders-ack}")
    private String labOrdersAckTopic;

    @Value("${spring.kafka.topic.lab-results:lab-results}")
    private String labResultsTopic;

    @Value("${lis.grpc.message-receiver.host:localhost}")
    private String messageReceiverHost;

    @Value("${lis.grpc.message-receiver.port:9091}")
    private int messageReceiverPort;

    @Value("${spring.kafka.properties.sasl.jaas.config:}")
    private String kafkaJaasConfig;

    @Value("${spring.kafka.properties.security.protocol:PLAINTEXT}")
    private String kafkaSecurityProtocol;

    @Value("${spring.kafka.properties.sasl.mechanism:}")
    private String kafkaSaslMechanism;
}
