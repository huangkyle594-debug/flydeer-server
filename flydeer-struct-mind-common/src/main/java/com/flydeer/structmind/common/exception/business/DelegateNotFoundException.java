package com.flydeer.structmind.common.exception.business;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class DelegateNotFoundException extends BusinessException {

    public DelegateNotFoundException() {
        super(ErrorCodes.DELEGATE_NOT_FOUND, "授权记录不存在");
    }
}
