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

    private String secret = "change-me-to-a-long-random-secret-key!!";
    private Duration accessTokenTtl = Duration.ofHours(2);
    private Duration refreshTokenTtl = Duration.ofDays(90);

    private String refreshCookieName = "refresh_token";
    private Boolean refreshCookieSecure = false;
    private String refreshCookiePath = "/api/v1/auth";

    private String redirectUrl = "http://localhost:5173/oauth/callback";
}
