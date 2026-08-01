package com.flydeer.structmind.common.exception.frequency;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class LoginFrequencyException extends RateLimitException {

    public LoginFrequencyException() {
        super(ErrorCodes.LOGIN_RATE_FREQUENCY, "登陆频繁，请稍后再试");
    }
}
