package com.flydeer.service.user.config;

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

    private Duration smsInterval;
    private int smsDailyLimitPerPhone;
    private int smsDailyLimitPerIp;
    private Duration loginInterval;
}
