package com.flydeer.common.exception.auth;

import com.flydeer.common.exception.ErrorCodes;

public class NeedLoginException extends AuthorizedException {

    public NeedLoginException() {
        super(ErrorCodes.NEED_LOGIN, "需要登陆态");
    }
}
