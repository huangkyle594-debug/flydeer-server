package com.flydeer.common.exception.business;

import com.flydeer.common.exception.ErrorCodes;

public class PhoneChannelOperateException extends BusinessException {

    public PhoneChannelOperateException() {
        super(ErrorCodes.PHONE_CHANNEL_OPERATE, "手机号登录无法完成此操作");
    }
}
