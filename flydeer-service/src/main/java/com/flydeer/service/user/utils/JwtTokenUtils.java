package com.flydeer.service.user.utils;

import com.flydeer.common.exception.auth.AccessTokenParseException;
import com.flydeer.common.exception.auth.RefreshTokenParseException;
import com.flydeer.contract.user.enums.UserStatusEnum;
import com.flydeer.service.user.config.JwtTokenConfig;
import com.flydeer.service.user.model.AccessTokenClaims;
import com.flydeer.service.user.model.IssuedTokensRecord;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenUtils {

    public static final String TYP_ACCESS = "access";
    public static final String TYP_REFRESH = "refresh";
    public static final String CLAIM_VERIFIED = "verified";
    public static final String CLAIM_STATUS = "status";
    public static final String CLAIM_NAME = "name";

    private final JwtTokenConfig jwtTokenConfig;

    public JwtTokenUtils(JwtTokenConfig jwtTokenConfig) {
        this.jwtTokenConfig = jwtTokenConfig;
    }

    public IssuedTokensRecord issue(long userId, boolean verified, Integer status, String name) {
        Instant now = Instant.now();
        Instant accessExp = now.plus(jwtTokenConfig.getAccessTokenTtl());
        Instant refreshExp = now.plus(jwtTokenConfig.getRefreshTokenTtl());
        int resolvedStatus = status != null ? status : UserStatusEnum.STATUS_ACTIVE.getCode();
        String access = buildAccessToken(userId, verified, resolvedStatus, name, now, accessExp);
        String refresh = buildRefreshToken(userId, now, refreshExp);
        return new IssuedTokensRecord(access, refresh, jwtTokenConfig.getAccessTokenTtl().toSeconds());
    }

    private String buildAccessToken(
        long userId, boolean verified, int status, String name, Instant iat, Instant exp) {
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("typ", TYP_ACCESS)
            .claim(CLAIM_VERIFIED, verified)
            .claim(CLAIM_STATUS, status)
            .claim(CLAIM_NAME, name)
            .issuedAt(Date.from(iat))
            .expiration(Date.from(exp))
            .signWith(secretKey())
            .compact();
    }

    private String buildRefreshToken(long userId, Instant iat, Instant exp) {
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("typ", TYP_REFRESH)
            .issuedAt(Date.from(iat))
            .expiration(Date.from(exp))
            .signWith(secretKey())
            .compact();
    }

    public AccessTokenClaims parseAccessToken(String token) throws AccessTokenParseException {
        try {
            Claims claims = parseClaims(token, TYP_ACCESS);
            long userId = Long.parseLong(claims.getSubject());
            Boolean verified = claims.get(CLAIM_VERIFIED, Boolean.class);
            Integer status = claims.get(CLAIM_STATUS, Integer.class);
            String name = claims.get(CLAIM_NAME, String.class);
            return new AccessTokenClaims(userId, Boolean.TRUE.equals(verified), status, name);
        } catch (Exception e) {
            throw new AccessTokenParseException();
        }
    }

    public Long parseRefreshToken(String token) throws RefreshTokenParseException {
        try {
            Claims claims = parseClaims(token, TYP_REFRESH);
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            throw new RefreshTokenParseException();
        }
    }

    private Claims parseClaims(String token, String expectedType) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        String typ = claims.get("typ", String.class);
        Assert.isTrue(expectedType.equals(typ), "token type error");
        return claims;
    }

    private SecretKey secretKey() {
        byte[] bytes = jwtTokenConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}
