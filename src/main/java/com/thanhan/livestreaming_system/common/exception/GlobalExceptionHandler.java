package com.thanhan.livestreaming_system.common.exception;

import com.thanhan.livestreaming_system.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException ex) {

        return ResponseEntity.badRequest().body(ApiResponse.builder()
                .message(ex.getMessage())
                .status(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .build());
    }

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handleException(Exception ex) {

        return ResponseEntity.badRequest().body(ApiResponse.builder()
                .message(ex.getMessage())
                .status(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String enumKey = ex.getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_MESSAGE_KEY;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {

        }

        return ResponseEntity.badRequest().body(ApiResponse.builder()
                .message(errorCode.getMessage())
                .status(errorCode.getCode())
                .build());

    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        return ResponseEntity.badRequest().body(ApiResponse.builder()
                .status(errorCode.getCode())
                .message(errorCode.getMessage()).
                build());
    }

    @ExceptionHandler(value = VnpPaymentException.class)
    ResponseEntity<ApiResponse> handleVnpPaymentException(VnpPaymentException ex) {

        VnpErrorCode errorCode = ex.getVnpErrorCode();

        return ResponseEntity.badRequest()
                .body(ApiResponse.builder()
                .status(errorCode.getCode())
                .message(errorCode.getMessage()).
                build());
    }
}
