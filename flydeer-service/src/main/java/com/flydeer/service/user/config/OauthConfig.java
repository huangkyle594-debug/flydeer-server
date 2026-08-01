package com.flydeer.service.user.config;

import com.flydeer.service.user.model.OauthProviderPojo;
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

    private String secret;
    private Long timeout;
    private Map<String, OauthProviderPojo> oauth = new HashMap<>();

    public OauthProviderPojo get(String provider) {
        return oauth.get(provider);
    }
}
