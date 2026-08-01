package com.flydeer.service.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.time.Duration;

@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "app.jwt")
public class JwtTokenConfig {

    private String secret;
    private Duration accessTokenTtl;
    private Duration refreshTokenTtl;

    private String refreshCookieName;
    private Boolean refreshCookieSecure;
    private String refreshCookiePath;

    private String redirectUrl;
}
