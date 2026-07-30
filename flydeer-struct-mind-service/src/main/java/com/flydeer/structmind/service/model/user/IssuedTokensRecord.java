package com.flydeer.structmind.service.model.user;

public record IssuedTokensRecord(String accessToken, String refreshToken, long expiresInSeconds) {
}
