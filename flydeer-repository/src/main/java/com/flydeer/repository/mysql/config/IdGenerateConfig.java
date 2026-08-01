package com.flydeer.repository.mysql.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "app.id")
public class IdGenerateConfig {
    private long start;
    private int stepMin;
    private int stepMax;
}
