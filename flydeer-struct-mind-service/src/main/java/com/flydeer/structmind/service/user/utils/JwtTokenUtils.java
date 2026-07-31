package com.flydeer.structmind.service.user.utils;

import com.flydeer.structmind.common.exception.auth.AccessTokenParseException;
import com.flydeer.structmind.common.exception.auth.RefreshTokenParseException;
import com.flydeer.structmind.service.user.config.JwtTokenConfig;
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

    private final JwtTokenConfig jwtTokenConfig;

    public JwtTokenUtils(JwtTokenConfig jwtTokenConfig) {
        this.jwtTokenConfig = jwtTokenConfig;
    }

    public IssuedTokensRecord issue(long userId) {
        Instant now = Instant.now();
        Instant accessExp = now.plus(jwtTokenConfig.getAccessTokenTtl());
        Instant refreshExp = now.plus(jwtTokenConfig.getRefreshTokenTtl());
        String access = buildToken(userId, TYP_ACCESS, now, accessExp);
        String refresh = buildToken(userId, TYP_REFRESH, now, refreshExp);
        return new IssuedTokensRecord(access, refresh, jwtTokenConfig.getAccessTokenTtl().toSeconds());
    }

    private String buildToken(long userId, String typ, Instant iat, Instant exp) {
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("typ", typ)
            .issuedAt(Date.from(iat))
            .expiration(Date.from(exp))
            .signWith(secretKey())
            .compact();
    }

    public Long parseAccessToken(String token) throws AccessTokenParseException {
        try {
            return parseUserId(token, TYP_ACCESS);
        } catch (Exception e) {
            throw new AccessTokenParseException();
        }
    }

    public Long parseRefreshToken(String token) throws RefreshTokenParseException {
        try {
            return parseUserId(token, TYP_REFRESH);
        } catch (Exception e) {
            throw new RefreshTokenParseException();
        }
    }

    private long parseUserId(String token, String expectedType) {
        Claims claims = Jwts.parser()
            .verifyWith(secretKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        String typ = claims.get("typ", String.class);
        Assert.isTrue(expectedType.equals(typ), "token type error");
        return Long.parseLong(claims.getSubject());
    }

    private SecretKey secretKey() {
        byte[] bytes = jwtTokenConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }
}
