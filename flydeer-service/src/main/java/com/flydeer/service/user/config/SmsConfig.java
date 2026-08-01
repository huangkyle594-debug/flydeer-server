package com.flydeer.service.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "app.sms")
public class SmsConfig {

    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    private String templateCode;
    private String region;
    private String endpoint;
    private String countryCode;
    private boolean mockEnabled;
}
