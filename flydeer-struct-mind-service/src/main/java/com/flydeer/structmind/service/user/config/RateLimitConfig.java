package com.flydeer.structmind.service.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.Duration;

@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "app.user.ratelimit")
public class RateLimitConfig {

    private Duration smsInterval = Duration.ofSeconds(60);
    private int smsDailyLimitPerPhone = 20;
    private int smsDailyLimitPerIp = 50;
    private Duration loginInterval = Duration.ofSeconds(1);
}
