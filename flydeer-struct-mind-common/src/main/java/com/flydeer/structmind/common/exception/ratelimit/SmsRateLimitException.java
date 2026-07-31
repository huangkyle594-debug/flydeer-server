package com.flydeer.structmind.common.exception.ratelimit;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class SmsRateLimitException extends RateLimitException {

    public SmsRateLimitException() {
        super(ErrorCodes.SMS_RATE_LIMIT, "短信验证码发送频繁，请稍后再试");
    }
}
