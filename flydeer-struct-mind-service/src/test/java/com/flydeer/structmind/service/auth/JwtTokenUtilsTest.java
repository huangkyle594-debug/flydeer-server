package com.flydeer.structmind.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.flydeer.structmind.common.exception.business.BusinessException;
import com.flydeer.structmind.service.config.AppAuthProperties;
import java.time.Duration;

import com.flydeer.structmind.service.utils.JwtTokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenUtilsTest {

    private JwtTokenUtils jwtTokenUtils;

    @BeforeEach
    void setUp() {
        AppAuthProperties properties = new AppAuthProperties();
        properties.getAuth().setJwtSecret("test-jwt-secret-key-at-least-32-bytes!!");
        properties.getAuth().setAccessTokenTtl(Duration.ofHours(2));
        properties.getAuth().setRefreshTokenTtl(Duration.ofDays(90));
        //jwtTokenUtils = new JwtTokenUtils(properties);
    }

    @Test
    void issueAndParseAccessToken() {
        JwtTokenUtils.IssuedTokens tokens = jwtTokenUtils.issue(10000001L);
        long userId = jwtTokenUtils.parseAccessToken(tokens.accessToken());
        assertEquals(10000001L, userId);
        assertEquals(7200L, tokens.expiresInSeconds());
    }

    @Test
    void rejectAccessAsRefresh() {
        JwtTokenUtils.IssuedTokens tokens = jwtTokenUtils.issue(10000001L);
        assertThrows(
                BusinessException.class,
                () -> jwtTokenUtils.parseRefreshToken(tokens.accessToken()));
    }
}
