package com.flydeer.structmind.common.utils;

import org.springframework.util.StringUtils;

public class PhoneNumberUtils {

    public static String maskPhone(String phone) {
        if (!StringUtils.hasText(phone) || phone.length() < 7) {
            return "user";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
