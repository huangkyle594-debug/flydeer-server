package com.flydeer.structmind.service.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "app.id")
public class IdGenerateConfig {
    private long start = 10_000_000L;
    private int stepMin = 1;
    private int stepMax = 99;
}
