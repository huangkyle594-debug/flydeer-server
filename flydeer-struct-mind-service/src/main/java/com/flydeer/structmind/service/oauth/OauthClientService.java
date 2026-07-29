package com.flydeer.structmind.service.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flydeer.structmind.common.error.ErrorCodes;
import com.flydeer.structmind.common.exception.BusinessException;
import com.flydeer.structmind.contract.enums.LoginChannel;
import com.flydeer.structmind.service.config.AppAuthProperties;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class OauthClientService {

    private final AppAuthProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OauthClientService(
            AppAuthProperties properties, RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String buildAuthorizeUrl(LoginChannel channel) {
        AppAuthProperties.OauthProvider provider = requireProvider(channel);
        String state = signState(channel.name() + ":" + System.currentTimeMillis());
        return provider.getAuthorizeUrl()
                + "?client_id="
                + enc(provider.getClientId())
                + "&redirect_uri="
                + enc(provider.getRedirectUri())
                + "&response_type=code"
                + "&state="
                + enc(state)
                + (StringUtils.hasText(provider.getScope()) ? "&scope=" + enc(provider.getScope()) : "");
    }

    public void validateState(String state) {
        if (!StringUtils.hasText(state) || !state.contains(".")) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "invalid oauth state");
        }
        String[] parts = state.split("\\.", 2);
        String payload = parts[0];
        String signature = parts[1];
        if (!hmac(payload).equals(signature)) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "invalid oauth state signature");
        }
        String[] payloadParts = payload.split(":", 2);
        if (payloadParts.length != 2) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "invalid oauth state payload");
        }
        long ts;
        try {
            ts = Long.parseLong(payloadParts[1]);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "invalid oauth state timestamp");
        }
        if (System.currentTimeMillis() - ts > 10 * 60 * 1000L) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "oauth state expired");
        }
    }

    public OauthUserInfo exchange(LoginChannel channel, String code) {
        AppAuthProperties.OauthProvider provider = requireProvider(channel);
        String accessToken = fetchAccessToken(channel, provider, code);
        return fetchUser(channel, provider, accessToken);
    }

    private String fetchAccessToken(
            LoginChannel channel, AppAuthProperties.OauthProvider provider, String code) {
        try {
            if (channel == LoginChannel.GITHUB) {
                String body = restClient
                        .post()
                        .uri(provider.getTokenUrl())
                        .header("Accept", "application/json")
                        .body(Map.of(
                                "client_id",
                                provider.getClientId(),
                                "client_secret",
                                provider.getClientSecret(),
                                "code",
                                code,
                                "redirect_uri",
                                provider.getRedirectUri()))
                        .retrieve()
                        .body(String.class);
                JsonNode node = objectMapper.readTree(body);
                String token = text(node, "access_token");
                if (!StringUtils.hasText(token)) {
                    throw new BusinessException(ErrorCodes.BAD_REQUEST, "oauth token missing");
                }
                return token;
            }
            // Gitee
            String body = restClient
                    .post()
                    .uri(provider.getTokenUrl()
                            + "?grant_type=authorization_code"
                            + "&client_id="
                            + enc(provider.getClientId())
                            + "&client_secret="
                            + enc(provider.getClientSecret())
                            + "&code="
                            + enc(code)
                            + "&redirect_uri="
                            + enc(provider.getRedirectUri()))
                    .retrieve()
                    .body(String.class);
            JsonNode node = objectMapper.readTree(body);
            String token = text(node, "access_token");
            if (!StringUtils.hasText(token)) {
                throw new BusinessException(ErrorCodes.BAD_REQUEST, "oauth token missing");
            }
            return token;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "oauth token exchange failed");
        }
    }

    private OauthUserInfo fetchUser(
            LoginChannel channel, AppAuthProperties.OauthProvider provider, String accessToken) {
        try {
            String body;
            if (channel == LoginChannel.GITHUB) {
                body = restClient
                        .get()
                        .uri(provider.getUserUrl())
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Accept", "application/vnd.github+json")
                        .retrieve()
                        .body(String.class);
            } else {
                body = restClient
                        .get()
                        .uri(provider.getUserUrl() + "?access_token=" + enc(accessToken))
                        .retrieve()
                        .body(String.class);
            }
            JsonNode node = objectMapper.readTree(body);
            String uid = firstNonBlank(text(node, "id"), text(node, "login"));
            String nickname = firstNonBlank(text(node, "name"), text(node, "login"), "user");
            if (!StringUtils.hasText(uid)) {
                throw new BusinessException(ErrorCodes.BAD_REQUEST, "oauth user id missing");
            }
            return new OauthUserInfo(uid, nickname);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "oauth user fetch failed");
        }
    }

    private AppAuthProperties.OauthProvider requireProvider(LoginChannel channel) {
        AppAuthProperties.OauthProvider provider =
                properties.getOauth().get(channel.name().toLowerCase(Locale.ROOT));
        if (provider == null || !StringUtils.hasText(provider.getClientId())) {
            throw new BusinessException(ErrorCodes.BAD_REQUEST, "oauth provider not configured");
        }
        return provider;
    }

    private String signState(String payload) {
        return payload + "." + hmac(payload);
    }

    private String hmac(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    properties.getAuth().getJwtSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("hmac failed", ex);
        }
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
