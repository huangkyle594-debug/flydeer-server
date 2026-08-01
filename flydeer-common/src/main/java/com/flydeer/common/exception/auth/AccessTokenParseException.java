package com.flydeer.common.exception.auth;

import com.flydeer.common.exception.ErrorCodes;

public class AccessTokenParseException extends AuthorizedException {

    public AccessTokenParseException() {
        super(ErrorCodes.AUTH_ACCESS_TOKEN, "鉴权失败");
    }
}
