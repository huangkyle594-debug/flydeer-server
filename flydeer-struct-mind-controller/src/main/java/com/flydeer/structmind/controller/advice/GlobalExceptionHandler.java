package com.flydeer.structmind.controller.advice;

import com.flydeer.structmind.common.error.ErrorCodes;
import com.flydeer.structmind.common.exception.BusinessException;
import com.flydeer.structmind.common.result.ApiResult;
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

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(BusinessException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case ErrorCodes.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case ErrorCodes.FORBIDDEN -> HttpStatus.FORBIDDEN;
            case ErrorCodes.NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ErrorCodes.CONFLICT -> HttpStatus.CONFLICT;
            case ErrorCodes.TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResult.fail(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResult<Void>> handleValidation(Exception ex) {
        return ResponseEntity.badRequest().body(ApiResult.fail(400, "validation failed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleOther(Exception ex) {
        log.error("Unhandled error", ex);
        return ResponseEntity.internalServerError()
                .body(ApiResult.fail(500, "internal server error"));
    }
}
