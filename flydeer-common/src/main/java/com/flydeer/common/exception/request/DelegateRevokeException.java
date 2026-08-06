package com.flydeer.common.exception.request;

import com.flydeer.common.exception.ErrorCodes;

public class DelegateRevokeException extends BadRequestException {

    public DelegateRevokeException() {
        super(ErrorCodes.DELEGATE_REVOKE, "身份不能为空");
    }
}
