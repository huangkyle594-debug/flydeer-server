package com.flydeer.common.exception.business;

import com.flydeer.common.exception.ErrorCodes;

public class UserInvalidException extends BusinessException {

    public UserInvalidException() {
        super(ErrorCodes.USER_INVALID, "用户已被禁用");
    }
}
