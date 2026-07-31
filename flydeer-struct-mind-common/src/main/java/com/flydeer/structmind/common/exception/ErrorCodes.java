package com.flydeer.structmind.common.exception;

/**
 * Shared business error codes.
 */
public final class ErrorCodes {


    public static final int AUTH = 40000;
    public static final int AUTH_ACCESS_TOKEN = 40010;
    public static final int AUTH_REFRESH_TOKEN = 40020;
    public static final int OAUTH_URL_BUILD = 40030;
    public static final int OAUTH_VALIDATE = 40040;
    public static final int OAUTH_EXCHANGE = 40050;
    public static final int SMS_SEND = 40060;
    public static final int SMS_VERIFY = 40070;


    public static final int RATE_LIMIT = 90000;
    public static final int SMS_RATE_LIMIT = 90010;
    public static final int LOGIN_RATE_LIMIT = 90020;

    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    public static final int TOO_MANY_REQUESTS = 429;
    public static final int BAD_REQUEST = 400;

    private ErrorCodes() {
    }
}
