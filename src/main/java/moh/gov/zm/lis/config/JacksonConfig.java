package moh.gov.zm.lis.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Provides the application {@link ObjectMapper}. Boot 4's modular starters do not
 * pull Jackson auto-configuration in via the WebFlux starter here, so this bean
 * is what backs both JSON (de)serialization for WebFlux codecs and every injected
 * {@code ObjectMapper}. Settings mirror the intent of the {@code spring.jackson.*}
 * block in {@code application.yaml}.
 */
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setTimeZone(TimeZone.getTimeZone("UTC"));
    }
}
