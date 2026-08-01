package com.flydeer.common.exception.auth;

import com.flydeer.common.exception.ErrorCodes;

public class SmsSendException extends AuthorizedException {

    public SmsSendException() {
        super(ErrorCodes.SMS_SEND, "发送短信验证码失败");
    }
}
