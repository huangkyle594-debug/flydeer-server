package com.flydeer.structmind.common.exception.frequency;

import lombok.Getter;

@Getter
public class FrequencyException extends Exception {

    private final int code;

    public FrequencyException(int code, String message) {
        super(message);
        this.code = code;
    }
}
