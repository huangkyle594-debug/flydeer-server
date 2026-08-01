package com.flydeer.structmind.common.exception.business;

import lombok.Getter;

/**
 * Base domain/business exception.
 */
@Getter
public class BusinessException extends Exception {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

}
