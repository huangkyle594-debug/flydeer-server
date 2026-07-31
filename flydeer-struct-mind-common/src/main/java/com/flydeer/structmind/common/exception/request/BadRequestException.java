package com.flydeer.structmind.common.exception.request;

import lombok.Getter;

@Getter
public class BadRequestException extends Exception {

    private final int code;

    public BadRequestException(int code, String message) {
        super(message);
        this.code = code;
    }
}
