package com.flydeer.service.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "app.common")
public class CommonConfig {

    /**
     * Site-wide notice text. Hot-refresh via {@code /actuator/refresh}.
     */
    private String notice;
}
