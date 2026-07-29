package com.flydeer.structmind.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.flydeer.structmind.common.exception.BusinessException;
import com.flydeer.structmind.service.config.AppAuthProperties;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        AppAuthProperties properties = new AppAuthProperties();
        properties.getAuth().setJwtSecret("test-jwt-secret-key-at-least-32-bytes!!");
        properties.getAuth().setAccessTokenTtl(Duration.ofHours(2));
        properties.getAuth().setRefreshTokenTtl(Duration.ofDays(90));
        jwtTokenService = new JwtTokenService(properties);
    }

    @Test
    void issueAndParseAccessToken() {
        JwtTokenService.IssuedTokens tokens = jwtTokenService.issue(10000001L);
        long userId = jwtTokenService.parseUserId(tokens.accessToken(), JwtTokenService.TYP_ACCESS);
        assertEquals(10000001L, userId);
        assertEquals(7200L, tokens.expiresInSeconds());
    }

    @Test
    void rejectAccessAsRefresh() {
        JwtTokenService.IssuedTokens tokens = jwtTokenService.issue(10000001L);
        assertThrows(
                BusinessException.class,
                () -> jwtTokenService.parseUserId(tokens.accessToken(), JwtTokenService.TYP_REFRESH));
    }
}
