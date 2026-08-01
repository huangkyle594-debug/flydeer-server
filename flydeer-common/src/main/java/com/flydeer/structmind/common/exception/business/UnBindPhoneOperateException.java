package com.flydeer.structmind.common.exception.business;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class UnBindPhoneOperateException extends BusinessException {

    public UnBindPhoneOperateException() {
        super(ErrorCodes.UN_BIND_PHONE_OPERATE, "未绑定手机号实名不能完成此操作");
    }
}
