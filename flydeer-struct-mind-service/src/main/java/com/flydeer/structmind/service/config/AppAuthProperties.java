package com.flydeer.structmind.service.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties(prefix = "app")
public class AppAuthProperties {

    private final Auth auth = new Auth();
    private final Id id = new Id();
    private final Sms sms = new Sms();
    private final RateLimit rateLimit = new RateLimit();
    private final Map<String, OauthProvider> oauth = new HashMap<>();

    public Auth getAuth() {
        return auth;
    }

    public Id getId() {
        return id;
    }

    public Sms getSms() {
        return sms;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public Map<String, OauthProvider> getOauth() {
        return oauth;
    }

    public static class Auth {
        private String jwtSecret = "change-me-to-a-long-random-secret-key!!";
        private Duration accessTokenTtl = Duration.ofHours(2);
        private Duration refreshTokenTtl = Duration.ofDays(90);
        private String refreshCookieName = "refresh_token";
        private String frontendRedirectUrl = "http://localhost:3000/auth/callback";
        private boolean refreshCookieSecure = false;
        private String refreshCookiePath = "/api/v1/auth";

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public Duration getAccessTokenTtl() {
            return accessTokenTtl;
        }

        public void setAccessTokenTtl(Duration accessTokenTtl) {
            this.accessTokenTtl = accessTokenTtl;
        }

        public Duration getRefreshTokenTtl() {
            return refreshTokenTtl;
        }

        public void setRefreshTokenTtl(Duration refreshTokenTtl) {
            this.refreshTokenTtl = refreshTokenTtl;
        }

        public String getRefreshCookieName() {
            return refreshCookieName;
        }

        public void setRefreshCookieName(String refreshCookieName) {
            this.refreshCookieName = refreshCookieName;
        }

        public String getFrontendRedirectUrl() {
            return frontendRedirectUrl;
        }

        public void setFrontendRedirectUrl(String frontendRedirectUrl) {
            this.frontendRedirectUrl = frontendRedirectUrl;
        }

        public boolean isRefreshCookieSecure() {
            return refreshCookieSecure;
        }

        public void setRefreshCookieSecure(boolean refreshCookieSecure) {
            this.refreshCookieSecure = refreshCookieSecure;
        }

        public String getRefreshCookiePath() {
            return refreshCookiePath;
        }

        public void setRefreshCookiePath(String refreshCookiePath) {
            this.refreshCookiePath = refreshCookiePath;
        }
    }

    public static class Id {
        private long start = 10_000_000L;
        private int stepMin = 1;
        private int stepMax = 99;

        public long getStart() {
            return start;
        }

        public void setStart(long start) {
            this.start = start;
        }

        public int getStepMin() {
            return stepMin;
        }

        public void setStepMin(int stepMin) {
            this.stepMin = stepMin;
        }

        public int getStepMax() {
            return stepMax;
        }

        public void setStepMax(int stepMax) {
            this.stepMax = stepMax;
        }
    }

    public static class Sms {
        private String accessKeyId = "";
        private String accessKeySecret = "";
        private String signName = "";
        private String templateCode = "";
        private String region = "cn-shanghai";
        private String endpoint = "dypnsapi.aliyuncs.com";
        private String countryCode = "86";
        /** When true (default if keys empty), skip Aliyun and accept any code in local/dev. */
        private boolean mockEnabled = true;

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public String getSignName() {
            return signName;
        }

        public void setSignName(String signName) {
            this.signName = signName;
        }

        public String getTemplateCode() {
            return templateCode;
        }

        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public boolean isMockEnabled() {
            return mockEnabled;
        }

        public void setMockEnabled(boolean mockEnabled) {
            this.mockEnabled = mockEnabled;
        }
    }

    public static class RateLimit {
        private Duration smsInterval = Duration.ofSeconds(60);
        private int smsDailyLimitPerPhone = 20;
        private int smsDailyLimitPerIp = 50;
        private Duration loginInterval = Duration.ofSeconds(1);

        public Duration getSmsInterval() {
            return smsInterval;
        }

        public void setSmsInterval(Duration smsInterval) {
            this.smsInterval = smsInterval;
        }

        public int getSmsDailyLimitPerPhone() {
            return smsDailyLimitPerPhone;
        }

        public void setSmsDailyLimitPerPhone(int smsDailyLimitPerPhone) {
            this.smsDailyLimitPerPhone = smsDailyLimitPerPhone;
        }

        public int getSmsDailyLimitPerIp() {
            return smsDailyLimitPerIp;
        }

        public void setSmsDailyLimitPerIp(int smsDailyLimitPerIp) {
            this.smsDailyLimitPerIp = smsDailyLimitPerIp;
        }

        public Duration getLoginInterval() {
            return loginInterval;
        }

        public void setLoginInterval(Duration loginInterval) {
            this.loginInterval = loginInterval;
        }
    }

    public static class OauthProvider {
        private String clientId = "";
        private String clientSecret = "";
        private String redirectUri = "";
        private String authorizeUrl = "";
        private String tokenUrl = "";
        private String userUrl = "";
        private String scope = "";

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }

        public String getAuthorizeUrl() {
            return authorizeUrl;
        }

        public void setAuthorizeUrl(String authorizeUrl) {
            this.authorizeUrl = authorizeUrl;
        }

        public String getTokenUrl() {
            return tokenUrl;
        }

        public void setTokenUrl(String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        public String getUserUrl() {
            return userUrl;
        }

        public void setUserUrl(String userUrl) {
            this.userUrl = userUrl;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }
    }
}
