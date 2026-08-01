package com.flydeer.service.base.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flydeer.repository.mysql.config.IdGenerateConfig;
import com.flydeer.service.user.config.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
@EnableConfigurationProperties({IdGenerateConfig.class, JwtTokenConfig.class, OauthConfig.class, SmsConfig.class, RateLimitConfig.class})
public class ServiceConfiguration {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        JavaTimeModule module = new JavaTimeModule();
        module.addSerializer(LocalDateTime.class,
            new JsonSerializer<>() {
                @Override
                public void serialize(LocalDateTime value, JsonGenerator gen,
                                      SerializerProvider provider) throws IOException {
                    long timestamp = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    gen.writeNumber(timestamp);
                }
            }
        );
        module.addDeserializer(LocalDateTime.class,
            new JsonDeserializer<>() {
                @Override
                public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException {
                    long timestamp = p.getValueAsLong();
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        ZoneId.systemDefault());
                }
            }
        );
        mapper.registerModule(module);
        mapper.registerModule(new JavaTimeModule());
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        // mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);

        return new ObjectMapper();
    }
}
