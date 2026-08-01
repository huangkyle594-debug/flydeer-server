package com.flydeer.common.exception.auth;

import lombok.Getter;

@Getter
public class AuthorizedException extends Exception {

    private final int code;

    public AuthorizedException(int code, String message) {
        super(message);
        this.code = code;
    }
}
