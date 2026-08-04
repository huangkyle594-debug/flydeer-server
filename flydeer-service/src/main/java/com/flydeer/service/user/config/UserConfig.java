package com.flydeer.service.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "app.user")
public class UserConfig {

    /**
     * Global salt for SHA-256(phone + salt) phone hashing.
     */
    private String phoneHashSalt;

    /**
     * User ids allowed for {@code AuthRequiredLevel.ADMIN} endpoints.
     */
    private List<Long> adminIds = new ArrayList<>();

    public boolean isAdmin(Long userId) {
        return userId != null && !CollectionUtils.isEmpty(adminIds) && adminIds.contains(userId);
    }
}
