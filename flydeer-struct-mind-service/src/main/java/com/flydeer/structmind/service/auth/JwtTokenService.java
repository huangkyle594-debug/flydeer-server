package com.flydeer.structmind.service.auth;

import com.flydeer.structmind.common.error.ErrorCodes;
import com.flydeer.structmind.common.exception.BusinessException;
import com.flydeer.structmind.service.config.AppAuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    public static final String TYP_ACCESS = "access";
    public static final String TYP_REFRESH = "refresh";

    private final AppAuthProperties properties;

    public JwtTokenService(AppAuthProperties properties) {
        this.properties = properties;
    }

    public IssuedTokens issue(long userId) {
        Instant now = Instant.now();
        Instant accessExp = now.plus(properties.getAuth().getAccessTokenTtl());
        Instant refreshExp = now.plus(properties.getAuth().getRefreshTokenTtl());
        String access = build(userId, TYP_ACCESS, now, accessExp);
        String refresh = build(userId, TYP_REFRESH, now, refreshExp);
        return new IssuedTokens(access, refresh, properties.getAuth().getAccessTokenTtl().toSeconds());
    }

    public long parseUserId(String token, String expectedType) {
        Claims claims = parse(token);
        String typ = claims.get("typ", String.class);
        if (typ == null || !typ.equals(expectedType)) {
            throw new BusinessException(ErrorCodes.UNAUTHORIZED, "invalid token type");
        }
        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCodes.UNAUTHORIZED, "invalid token subject");
        }
    }

    public boolean isValid(String token, String expectedType) {
        try {
            parseUserId(token, expectedType);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String build(long userId, String typ, Instant iat, Instant exp) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("typ", typ)
                .issuedAt(Date.from(iat))
                .expiration(Date.from(exp))
                .signWith(secretKey())
                .compact();
    }

    private Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception ex) {
            throw new BusinessException(ErrorCodes.UNAUTHORIZED, "invalid or expired token");
        }
    }

    private SecretKey secretKey() {
        byte[] bytes = properties.getAuth().getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, 32));
            return Keys.hmacShaKeyFor(padded);
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    public record IssuedTokens(String accessToken, String refreshToken, long expiresInSeconds) {}
}
