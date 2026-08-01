package com.flydeer.structmind.common.exception.frequency;

import lombok.Getter;

@Getter
public class RateLimitException extends Exception {

    private final int code;

    public RateLimitException(int code, String message) {
        super(message);
        this.code = code;
    }
}
