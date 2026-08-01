package com.flydeer.structmind.controller.advice;

import com.flydeer.structmind.common.exception.ErrorCodes;
import com.flydeer.structmind.common.exception.auth.AuthorizedException;
import com.flydeer.structmind.common.exception.auth.NeedLoginException;
import com.flydeer.structmind.common.exception.auth.NeedVerifyException;
import com.flydeer.structmind.common.exception.business.BusinessException;
import com.flydeer.structmind.common.exception.frequency.RateLimitException;
import com.flydeer.structmind.common.exception.request.BadRequestException;
import com.flydeer.structmind.contract.base.response.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({NeedLoginException.class, NeedVerifyException.class})
    public ResponseEntity<ApiResult<Void>> handleAuthRequirement(AuthorizedException ex) {
        HttpStatus status = ex instanceof NeedVerifyException ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(AuthorizedException.class)
    public ResponseEntity<ApiResult<Void>> handleAuthorized(AuthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(BusinessException ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResult<Void>> handleRateLimit(RateLimitException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResult<Void>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResult<Void>> handleValidation(Exception ex) {
        return ResponseEntity.badRequest().body(ApiResult.fail(ErrorCodes.BAD_REQUEST, "validation failed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleOther(Exception ex) {
        log.error("Unhandled error", ex);
        return ResponseEntity.internalServerError()
            .body(ApiResult.fail(500, "internal server error"));
    }
}
