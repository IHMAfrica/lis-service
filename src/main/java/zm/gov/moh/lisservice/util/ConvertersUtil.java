package zm.gov.moh.lisservice.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import zm.gov.moh.lisservice.constant.Metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ConvertersUtil {
    @Bean
    public R2dbcCustomConversions customConversions(ObjectMapper objectMapper) {
        List<Converter<?, ?>> converters = new ArrayList<>();

        converters.add(new AuditLogsMetadataToJsonConverter(objectMapper));
        converters.add(new JsonToAuditLogsMetadataConverter(objectMapper));

        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
    }

    @WritingConverter
    @AllArgsConstructor
    public static class AuditLogsMetadataToJsonConverter implements Converter<Metadata.AuditLogsMetadata, Json> {
        private final ObjectMapper objectMapper;

        @Override
        public Json convert(Metadata.AuditLogsMetadata source) {
            try {
                return Json.of(objectMapper.writeValueAsString(source));
            } catch (IOException e) {
                throw new RuntimeException("Error writing JSON", e);
            }
        }
    }

    @ReadingConverter
    @AllArgsConstructor
    public static class JsonToAuditLogsMetadataConverter implements Converter<Json, Metadata.AuditLogsMetadata> {
        private final ObjectMapper objectMapper;

        @Override
        public Metadata.AuditLogsMetadata convert(Json source) {
            try {
                return objectMapper.readValue(source.asString(), Metadata.AuditLogsMetadata.class);
            } catch (IOException e) {
                throw new RuntimeException("Error reading JSON", e);
            }
        }
    }
}
