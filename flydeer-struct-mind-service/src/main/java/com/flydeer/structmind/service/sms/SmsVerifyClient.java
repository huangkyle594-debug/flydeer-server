package com.flydeer.structmind.service.sms;

public interface SmsVerifyClient {

    void sendVerifyCode(String phone);

    void checkVerifyCode(String phone, String code);
}
