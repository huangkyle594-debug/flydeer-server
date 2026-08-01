package com.flydeer.structmind.common.exception.business;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class UserInvalidException extends BusinessException {

    public UserInvalidException() {
        super(ErrorCodes.USER_INVALID, "用户已被禁用");
    }
}
