package com.flydeer.structmind.common.exception.auth;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class NeedLoginException extends AuthorizedException {

    public NeedLoginException() {
        super(ErrorCodes.NEED_LOGIN, "需要登陆态");
    }
}
