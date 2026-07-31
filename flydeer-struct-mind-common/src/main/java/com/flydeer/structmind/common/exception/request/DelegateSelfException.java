package com.flydeer.structmind.common.exception.request;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class DelegateSelfException extends BadRequestException {

    public DelegateSelfException() {
        super(ErrorCodes.DELEGATE_SELF, "不能授权给自己");
    }
}
