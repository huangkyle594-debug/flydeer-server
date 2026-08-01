package com.flydeer.structmind.common.exception.business;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(ErrorCodes.USER_NOT_FOUND, "无此用户");
    }
}
