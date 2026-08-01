package com.flydeer.structmind.common.exception;

/**
 * Shared business error codes.
 */
public final class ErrorCodes {

    public static final int SUCCESS = 0;

    public static final int AUTH = 30000;
    public static final int AUTH_ACCESS_TOKEN = 30010;
    public static final int AUTH_REFRESH_TOKEN = 30020;
    public static final int OAUTH_URL_BUILD = 30030;
    public static final int OAUTH_VALIDATE = 30040;
    public static final int OAUTH_EXCHANGE = 30050;
    public static final int SMS_SEND = 30060;
    public static final int SMS_VERIFY = 30070;

    public static final int NEED_LOGIN = 31010;
    public static final int NEED_VERIFY = 31020;

    public static final int BAD_REQUEST = 40000;
    public static final int DELEGATE_SELF = 41010;

    public static final int BUSINESS = 50000;

    public static final int ENTITY_NOT_FOUND = 51000;
    public static final int USER_NOT_FOUND = 51010;
    public static final int DELEGATE_NOT_FOUND = 51020;

    public static final int ENTITY_INVALID = 52000;
    public static final int USER_INVALID = 52010;

    public static final int BUSINESS_LIMIT = 53000;
    public static final int PHONE_BIND_LIMIT = 53010;

    public static final int BAD_OPERATE = 60000;
    public static final int PHONE_CHANNEL_OPERATE = 61010;
    public static final int UN_BIND_PHONE_OPERATE = 61020;

    public static final int FREQUENCY = 90000;
    public static final int SMS_RATE_FREQUENCY = 91010;
    public static final int LOGIN_RATE_FREQUENCY = 91020;

    public static final int UNKNOWN = 999999;

    private ErrorCodes() {
    }
}
