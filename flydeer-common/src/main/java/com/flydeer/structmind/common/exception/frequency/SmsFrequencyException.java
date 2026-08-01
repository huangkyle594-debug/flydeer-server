package com.flydeer.structmind.common.exception.frequency;

import com.flydeer.structmind.common.exception.ErrorCodes;

public class SmsFrequencyException extends FrequencyException {

    public SmsFrequencyException() {
        super(ErrorCodes.SMS_RATE_FREQUENCY, "短信验证码发送频繁，请稍后再试");
    }
}
