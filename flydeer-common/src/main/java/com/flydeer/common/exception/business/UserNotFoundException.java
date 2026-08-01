package com.flydeer.common.exception.business;

import com.flydeer.common.exception.ErrorCodes;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(ErrorCodes.USER_NOT_FOUND, "无此用户");
    }
}
