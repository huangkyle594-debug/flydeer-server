package com.flydeer.common.exception.request;

import com.flydeer.common.exception.ErrorCodes;
import lombok.Getter;

@Getter
public class BadRequestException extends Exception {

    private final int code;

    public BadRequestException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BadRequestException(String message) {
        super(message);
        this.code = ErrorCodes.BAD_REQUEST;
    }
}
