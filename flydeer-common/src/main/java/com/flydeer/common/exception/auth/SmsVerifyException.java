package com.flydeer.common.exception.auth;

import com.flydeer.common.exception.ErrorCodes;

public class SmsVerifyException extends AuthorizedException {

    public SmsVerifyException() {
        super(ErrorCodes.SMS_VERIFY, "短信验证码验证失败");
    }
}
