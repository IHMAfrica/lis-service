package moh.gov.zm.lis.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka wiring. Outbound HL7 messages are produced through a reactor-kafka
 * {@link KafkaSender} (the outbox relay); inbound HL7 (lab-order acknowledgements,
 * later lab results) is consumed via Kafka Streams — see the topology beans that
 * take the {@code StreamsBuilder}. The {@link AdminClient} is available for topic
 * administration and health checks.
 */
@Configuration
@EnableKafkaStreams
@RequiredArgsConstructor
public class KafkaConfig {
    private final ConfigProperties configProperties;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, configProperties.getKafkaStreamsApplicationId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, configProperties.getKafkaBootstrapServers());
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.SECURITY_PROTOCOL_CONFIG, configProperties.getKafkaSecurityProtocol());
        props.put("sasl.mechanism", configProperties.getKafkaSaslMechanism());
        props.put("sasl.jaas.config", configProperties.getKafkaJaasConfig());
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2);
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 200);
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.AT_LEAST_ONCE);
        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest");
        props.put(StreamsConfig.consumerPrefix(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG), 30000);

        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public AdminClient adminClient() {
        Map<String, Object> config = new HashMap<>();

        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, configProperties.getKafkaBootstrapServers());
        config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 5000);
        config.put(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, 10000);
        config.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, configProperties.getKafkaSecurityProtocol());
        config.put("sasl.mechanism", configProperties.getKafkaSaslMechanism());
        config.put("sasl.jaas.config", configProperties.getKafkaJaasConfig());

        return AdminClient.create(config);
    }

    @Bean
    public SenderOptions<String, String> senderOptions() {
        Map<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configProperties.getKafkaBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 280000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 80000);
        // Ordered, durable delivery for the outbox: full ISR acks + idempotent producer
        // so retries cannot reorder or duplicate HL7 messages on the topic.
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500);
        props.put("sasl.jaas.config", configProperties.getKafkaJaasConfig());
        props.put("security.protocol", configProperties.getKafkaSecurityProtocol());
        props.put("sasl.mechanism", configProperties.getKafkaSaslMechanism());

        return SenderOptions.create(props);
    }

    @Bean
    public KafkaSender<String, String> kafkaSender(SenderOptions<String, String> senderOptions) {
        return KafkaSender.create(senderOptions);
    }
}
