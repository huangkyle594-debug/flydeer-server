package com.flydeer.structmind.common.exception.auth;

import com.flydeer.structmind.common.error.ErrorCodes;

public class RefreshTokenParseException extends AuthorizedException {

    public RefreshTokenParseException() {
        super(ErrorCodes.AUTH_REFRESH_TOKEN, "登陆过期，请返回首页重新登陆");
    }
}
