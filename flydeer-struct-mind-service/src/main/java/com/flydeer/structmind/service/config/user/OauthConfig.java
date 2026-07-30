package com.flydeer.structmind.service.config.user;

import com.flydeer.structmind.service.model.user.OauthProviderPojo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "app.oauth")
public class OauthConfig {

    private String refreshCookieName = "refresh_token";
    private String frontendRedirectUrl = "http://localhost:3000/auth/callback";
    private boolean refreshCookieSecure = false;
    private String refreshCookiePath = "/api/v1/auth";

    private String secret = "";
    private Long timeout = 10 * 60 * 1000L;
    private Map<String, OauthProviderPojo> oauth = new HashMap<>();

    public OauthProviderPojo get(String provider) {
        return oauth.get(provider);
    }
}
