package com.flydeer.common.exception.business;

import com.flydeer.common.exception.ErrorCodes;

public class DelegateNotFoundException extends BusinessException {

    public DelegateNotFoundException() {
        super(ErrorCodes.DELEGATE_NOT_FOUND, "授权记录不存在");
    }
}
