package com.flydeer.common.exception.auth;

import com.flydeer.common.exception.ErrorCodes;

public class OauthValidateException extends AuthorizedException {

    public OauthValidateException() {
        super(ErrorCodes.OAUTH_VALIDATE, "第三方登录失败");
    }
}
