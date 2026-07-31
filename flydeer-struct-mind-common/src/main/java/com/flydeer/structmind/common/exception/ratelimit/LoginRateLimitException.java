package com.flydeer.structmind.common.exception.ratelimit;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class LoginRateLimitException extends RateLimitException {

    public LoginRateLimitException() {
        super(ErrorCodes.LOGIN_RATE_LIMIT, "登陆频繁，请稍后再试");
    }
}
