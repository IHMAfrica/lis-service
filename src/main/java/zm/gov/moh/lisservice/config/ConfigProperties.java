package zm.gov.moh.lisservice.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ConfigProperties {
    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${spring.keycloak.client-id}")
    private String keycloakClientId;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.jaas-config}")
    private String jaasConfig;

    @Value("${spring.kafka.topic.lab-order}")
    private String labOrderTopic;
}
