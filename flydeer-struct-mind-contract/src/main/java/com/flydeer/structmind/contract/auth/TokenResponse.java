package com.flydeer.structmind.contract.auth;

public record TokenResponse(String accessToken, long expiresInSeconds) {}
