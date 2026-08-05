package com.flydeer.controller.advice;

import com.flydeer.common.exception.ErrorCodes;
import com.flydeer.common.exception.auth.AuthorizedException;
import com.flydeer.common.exception.auth.NeedAdminException;
import com.flydeer.common.exception.auth.NeedLoginException;
import com.flydeer.common.exception.auth.NeedVerifyException;
import com.flydeer.common.exception.auth.SmsVerifyException;
import com.flydeer.common.exception.business.BusinessException;
import com.flydeer.common.exception.frequency.FrequencyException;
import com.flydeer.common.exception.request.BadRequestException;
import com.flydeer.contract.common.response.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NeedLoginException.class)
    public ResponseEntity<ApiResult<Void>> handleNeedLogin(NeedLoginException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({NeedVerifyException.class, NeedAdminException.class, SmsVerifyException.class})
    public ResponseEntity<ApiResult<Void>> handleAuthRequirement(AuthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(AuthorizedException.class)
    public ResponseEntity<ApiResult<Void>> handleAuthorized(AuthorizedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(FrequencyException.class)
    public ResponseEntity<ApiResult<Void>> handleRateLimit(FrequencyException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResult<Void>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResult<Void>> handleValidation(Exception ex) {
        return ResponseEntity.badRequest().body(ApiResult.fail(ErrorCodes.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleOther(Exception ex) {
        log.info("handleOther", ex);
        return ResponseEntity.internalServerError().body(ApiResult.fail(ErrorCodes.UNKNOWN, "系统异常"));
    }
}
