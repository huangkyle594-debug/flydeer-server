package com.flydeer.structmind.common.exception.auth;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class NeedVerifyException extends AuthorizedException {

    public NeedVerifyException() {
        super(ErrorCodes.NEED_VERIFY, "仅对实名用户开放");
    }
}
