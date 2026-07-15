package moh.gov.zm.lis;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Throwaway backing services for integration/context tests. The PostgreSQL
 * {@code @ServiceConnection} supplies both the R2DBC (runtime) and JDBC (Flyway)
 * connection details from a single container, so the real migrations run against
 * it; Redis is wired via a generic container matched on the {@code redis} image.
 *
 * <p>Kafka is intentionally not started here — it is an external dependency, and
 * the Kafka clients stay lazy while Streams auto-startup is disabled for tests
 * (see {@code application-test.yaml}).
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
    }
}
