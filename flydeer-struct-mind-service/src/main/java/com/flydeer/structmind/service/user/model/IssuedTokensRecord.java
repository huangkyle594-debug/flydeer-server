package com.flydeer.structmind.service.user.model;

public record IssuedTokensRecord(String accessToken, String refreshToken, long expiresInSeconds) {
}
