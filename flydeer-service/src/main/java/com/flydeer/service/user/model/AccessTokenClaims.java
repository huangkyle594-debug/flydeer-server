package com.flydeer.service.user.model;

public record AccessTokenClaims(long userId, boolean verified) {
}
