package com.flydeer.common.exception.request;

import com.flydeer.common.exception.ErrorCodes;

public class DelegateSelfException extends BadRequestException {

    public DelegateSelfException() {
        super(ErrorCodes.DELEGATE_SELF, "不能授权给自己");
    }
}
