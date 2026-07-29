package com.flydeer.structmind.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AppAuthProperties.class)
public class ServiceConfiguration {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}