package com.flydeer.structmind.service.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flydeer.structmind.common.exception.auth.OauthExchangeException;
import com.flydeer.structmind.common.exception.auth.OauthUrlBuildException;
import com.flydeer.structmind.common.exception.auth.OauthValidateException;
import com.flydeer.structmind.contract.user.enums.LoginChannel;
import com.flydeer.structmind.service.user.config.OauthConfig;
import com.flydeer.structmind.service.user.model.OauthProviderPojo;
import com.flydeer.structmind.service.user.model.OauthUserRecord;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;

@Service
public class OauthService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final OauthConfig oauthConfig;
    private final Map<LoginChannel, ThirdPlatformExchanger> exchangerMap = new HashMap<>();

    public OauthService(
        OauthConfig oauthConfig, RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.oauthConfig = oauthConfig;
        RestClient restClient = restClientBuilder.build();
        exchangerMap.put(LoginChannel.GITEE, new GiteeExchanger(objectMapper, restClient));
        exchangerMap.put(LoginChannel.GITHUB, new GithubExchanger(objectMapper, restClient));
    }

    public String buildAuthorizeUrl(LoginChannel channel) throws OauthUrlBuildException {
        try {
            OauthProviderPojo provider = oauthConfig.get(channel.name().toLowerCase(Locale.ROOT));
            String state = signState(channel.name() + ":" + System.currentTimeMillis());
            StringBuilder sb = new StringBuilder();
            sb.append(provider.getAuthorizeUrl())
                .append("?client_id=").append(enc(provider.getClientId()))
                .append("&redirect_uri").append(enc(provider.getRedirectUri()))
                .append("&response_type=code")
                .append("&state=").append(enc(state));
            if (StringUtils.hasText(provider.getScope())) {
                sb.append("&scope=").append(enc(provider.getScope()));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new OauthUrlBuildException();
        }
    }

    public void validateState(String state) throws OauthValidateException {
        try {
            String[] parts = state.split("\\.", 2);
            String payload = parts[0];
            String signature = parts[1];
            Assert.isTrue(hmac(payload).equals(signature), "invalid oauth state signature");
            String[] payloadParts = payload.split(":", 2);
            Assert.isTrue(payloadParts.length == 2, "invalid oauth state payload");
            long ts = Long.parseLong(payloadParts[1]);
            Assert.isTrue(System.currentTimeMillis() - ts > oauthConfig.getTimeout(), "oauth state expired");
        } catch (Exception e) {
            throw new OauthValidateException();
        }
    }

    public OauthUserRecord exchange(LoginChannel channel, String code) throws OauthExchangeException {
        try {
            OauthProviderPojo provider = oauthConfig.get(channel.name().toLowerCase(Locale.ROOT));
            ThirdPlatformExchanger exchanger = exchangerMap.get(channel);
            String accessToken = exchanger.fetchAccessToken(provider, code);
            return exchanger.fetchUser(provider, accessToken);
        } catch (Exception e) {
            throw new OauthExchangeException();
        }
    }

    private interface ThirdPlatformExchanger {
        ObjectMapper getMapper();

        default String fetchAccessToken(OauthProviderPojo provider, String code) throws JsonProcessingException {
            String body = tokenBody(provider, code);
            JsonNode node = getMapper().readTree(body);
            String token = text(node, "access_token");
            Assert.isTrue(StringUtils.hasText(token), "oauth token missing");
            return token;
        }

        String tokenBody(OauthProviderPojo provider, String code);

        default OauthUserRecord fetchUser(OauthProviderPojo provider, String accessToken) throws JsonProcessingException {
            String body = userBody(provider, accessToken);
            JsonNode node = getMapper().readTree(body);
            String uid = firstNonBlank(text(node, "id"), text(node, "login"));
            String username = firstNonBlank(text(node, "name"), text(node, "login"), "user");
            Assert.isTrue(StringUtils.hasText(uid), "oauth user id missing");
            return new OauthUserRecord(uid, username);
        }

        String userBody(OauthProviderPojo provider, String accessToken);
    }

    @AllArgsConstructor
    private static class GiteeExchanger implements ThirdPlatformExchanger {
        private ObjectMapper objectMapper;
        private RestClient restClient;

        @Override
        public ObjectMapper getMapper() {
            return objectMapper;
        }

        @Override
        public String tokenBody(OauthProviderPojo provider, String code) {
            return restClient.post()
                .uri(provider.getTokenUrl()
                    + "?grant_type=authorization_code"
                    + "&client_id=" + enc(provider.getClientId())
                    + "&client_secret=" + enc(provider.getClientSecret())
                    + "&code=" + enc(code)
                    + "&redirect_uri=" + enc(provider.getRedirectUri()))
                .retrieve()
                .body(String.class);
        }

        @Override
        public String userBody(OauthProviderPojo provider, String accessToken) {
            return restClient
                .get()
                .uri(provider.getUserUrl() + "?access_token=" + enc(accessToken))
                .retrieve()
                .body(String.class);
        }
    }

    @AllArgsConstructor
    private static class GithubExchanger implements ThirdPlatformExchanger {
        private ObjectMapper objectMapper;
        private RestClient restClient;

        @Override
        public ObjectMapper getMapper() {
            return objectMapper;
        }

        @Override
        public String tokenBody(OauthProviderPojo provider, String code) {
            return restClient.post()
                .uri(provider.getTokenUrl())
                .header("Accept", "application/json")
                .body(Map.of("client_id", provider.getClientId(),
                    "client_secret", provider.getClientSecret(),
                    "code", code,
                    "redirect_uri", provider.getRedirectUri()))
                .retrieve()
                .body(String.class);
        }

        @Override
        public String userBody(OauthProviderPojo provider, String accessToken) {
            return restClient.get()
                .uri(provider.getUserUrl())
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/vnd.github+json")
                .retrieve()
                .body(String.class);
        }
    }

    private String signState(String payload) throws NoSuchAlgorithmException, InvalidKeyException {
        return payload + "." + hmac(payload);
    }

    private String hmac(String payload) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(
            oauthConfig.getSecret().getBytes(StandardCharsets.UTF_8),
            HMAC_ALGORITHM
        );
        mac.init(secretKey);
        byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(raw);
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String text(JsonNode node, String field) {
        JsonNode child = node.get(field);
        return child == null || child.isNull() ? null : child.asText();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}
