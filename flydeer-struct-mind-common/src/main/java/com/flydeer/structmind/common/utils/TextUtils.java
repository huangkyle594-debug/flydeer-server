package com.flydeer.structmind.common.utils;

import org.springframework.util.StringUtils;

import java.util.UUID;

public class TextUtils {

    public static String trimText(String text, int length) {
        if (!StringUtils.hasText(text)) {
            return randomText(length / 2);
        }
        String trimmed = text.trim();
        return trimmed.length() > length ? trimmed.substring(0, length) : trimmed;
    }

    public static String randomText(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }
}
