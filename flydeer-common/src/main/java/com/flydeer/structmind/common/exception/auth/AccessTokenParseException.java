package com.flydeer.structmind.common.exception.auth;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class AccessTokenParseException extends AuthorizedException {

    public AccessTokenParseException() {
        super(ErrorCodes.AUTH_ACCESS_TOKEN, "鉴权失败");
    }
}
