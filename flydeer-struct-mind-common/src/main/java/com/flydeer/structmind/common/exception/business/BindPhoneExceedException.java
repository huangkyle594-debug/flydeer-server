package com.flydeer.structmind.common.exception.business;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class BindPhoneExceedException extends BusinessException {

    public BindPhoneExceedException() {
        super(ErrorCodes.PHONE_BIND_LIMIT, "手机号每种类型账号最多绑定一个");
    }
}
