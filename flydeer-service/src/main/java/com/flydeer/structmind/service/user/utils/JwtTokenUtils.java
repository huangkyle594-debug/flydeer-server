package com.flydeer.structmind.service.user.utils;

import com.flydeer.structmind.common.exception.auth.AccessTokenParseException;
import com.flydeer.structmind.common.exception.auth.RefreshTokenParseException;
import com.flydeer.structmind.service.user.config.JwtTokenConfig;
import com.flydeer.structmind.service.user.model.AccessTokenClaims;
import com.flydeer.structmind.service.user.model.IssuedTokensRecord;
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

    private final JwtTokenConfig jwtTokenConfig;

    public JwtTokenUtils(JwtTokenConfig jwtTokenConfig) {
        this.jwtTokenConfig = jwtTokenConfig;
    }

    public IssuedTokensRecord issue(long userId, boolean verified) {
        Instant now = Instant.now();
        Instant accessExp = now.plus(jwtTokenConfig.getAccessTokenTtl());
        Instant refreshExp = now.plus(jwtTokenConfig.getRefreshTokenTtl());
        String access = buildAccessToken(userId, verified, now, accessExp);
        String refresh = buildRefreshToken(userId, now, refreshExp);
        return new IssuedTokensRecord(access, refresh, jwtTokenConfig.getAccessTokenTtl().toSeconds());
    }

    private String buildAccessToken(long userId, boolean verified, Instant iat, Instant exp) {
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("typ", TYP_ACCESS)
            .claim(CLAIM_VERIFIED, verified)
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
            return new AccessTokenClaims(userId, Boolean.TRUE.equals(verified));
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
