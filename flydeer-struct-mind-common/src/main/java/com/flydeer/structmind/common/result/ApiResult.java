package com.flydeer.structmind.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Uniform API envelope used across modules.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(int code, String message, T data) {

    public static final int SUCCESS_CODE = 0;

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>(SUCCESS_CODE, "ok", data);
    }

    public static ApiResult<Void> ok() {
        return new ApiResult<>(SUCCESS_CODE, "ok", null);
    }

    public static ApiResult<Void> fail(int code, String message) {
        return new ApiResult<>(code, message, null);
    }
}
