package com.flydeer.common.exception.auth;

import com.flydeer.common.exception.ErrorCodes;

public class NeedAdminException extends AuthorizedException {

    public NeedAdminException() {
        super(ErrorCodes.NEED_ADMIN, "仅对管理员开放");
    }
}
