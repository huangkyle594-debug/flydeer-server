package com.flydeer.common.exception.frequency;

import com.flydeer.common.exception.ErrorCodes;

public class LoginFrequencyException extends FrequencyException {

    public LoginFrequencyException() {
        super(ErrorCodes.LOGIN_RATE_FREQUENCY, "登陆频繁，请稍后再试");
    }
}
