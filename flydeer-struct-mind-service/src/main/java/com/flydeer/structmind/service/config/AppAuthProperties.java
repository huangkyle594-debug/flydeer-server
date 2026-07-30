package com.flydeer.structmind.service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.Duration;

@Getter
@RefreshScope
@ConfigurationProperties(prefix = "app")
public class AppAuthProperties {

    private final RateLimit rateLimit = new RateLimit();

    @Setter
    @Getter
    public static class RateLimit {
        private Duration smsInterval = Duration.ofSeconds(60);
        private int smsDailyLimitPerPhone = 20;
        private int smsDailyLimitPerIp = 50;
        private Duration loginInterval = Duration.ofSeconds(1);

    }


}
