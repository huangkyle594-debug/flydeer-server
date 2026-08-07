package com.flydeer.contract.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.flydeer.common.exception.ErrorCodes;

/**
 * Uniform API envelope used across modules.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(int code, String message, T data) {

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(ErrorCodes.SUCCESS, "ok", data);
    }

    public static ApiResult<Void> ok() {
        return new ApiResult<>(ErrorCodes.SUCCESS, "ok", null);
    }

    public static ApiResult<Void> fail(int code, String message) {
        return new ApiResult<>(code, message, null);
    }

    public static <T> ApiResult<T> fail(int code, String message, T data) {
        return new ApiResult<>(code, message, data);
    }
}
