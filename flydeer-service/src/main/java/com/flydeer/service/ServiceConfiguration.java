package com.flydeer.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flydeer.repository.mysql.config.IdGenerateConfig;
import com.flydeer.service.common.config.CommonConfig;
import com.flydeer.service.user.config.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
@EnableAsync
@EnableConfigurationProperties({
    IdGenerateConfig.class,
    JwtTokenConfig.class,
    OauthConfig.class,
    SmsConfig.class,
    RateLimitConfig.class,
    UserConfig.class,
    CommonConfig.class})
public class ServiceConfiguration {

    @Bean
    public RestClient.Builder restClientBuilder() {
        // GitHub 等站点在 JDK HttpClient + HTTP/2 下偶发 EOF；强制 HTTP/1.1 更稳
        HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        return RestClient.builder().requestFactory(requestFactory);
    }

    @Bean
    @Primary
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
        module.addSerializer(Instant.class,
            new JsonSerializer<>() {
                @Override
                public void serialize(Instant value, JsonGenerator gen,
                                      SerializerProvider provider) throws IOException {
                    gen.writeNumber(value.toEpochMilli());
                }
            }
        );
        module.addDeserializer(Instant.class,
            new JsonDeserializer<>() {
                @Override
                public Instant deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException {
                    return Instant.ofEpochMilli(p.getValueAsLong());
                }
            }
        );
        mapper.registerModule(module);
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
