package com.flydeer.structmind.common.exception.auth;

import com.flydeer.structmind.common.error.ErrorCodes;

public class SmsVerifyException extends AuthorizedException {

    public SmsVerifyException() {
        super(ErrorCodes.SMS_VERIFY, "短信验证码验证失败");
    }
}
