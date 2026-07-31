package com.flydeer.structmind.contract.user.vo;

public record TokenResponse(String accessToken, long expiresInSeconds) {}
