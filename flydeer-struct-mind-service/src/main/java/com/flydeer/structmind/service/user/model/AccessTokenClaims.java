package com.flydeer.structmind.service.user.model;

public record AccessTokenClaims(long userId, boolean verified) {
}
