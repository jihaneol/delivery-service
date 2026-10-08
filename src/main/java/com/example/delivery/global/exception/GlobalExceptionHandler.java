package com.example.delivery.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(GlobalException e, HttpServletRequest request) {
        if (e.getStatus().is5xxServerError()) {
            log.error("요청 처리 실패 {} {} -> {}", request.getMethod(), request.getRequestURI(), e.getStatus().value(), e);
        } else {
            log.warn("요청 처리 실패 {} {} -> {} {}",
                    request.getMethod(), request.getRequestURI(), e.getStatus().value(), e.getMessage());
        }
        return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getMessage()));
    }

    // @Valid 검증 실패 - 첫 번째 메시지만 내려준다
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        log.warn("검증에 실패했습니다.  message = {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(message));
    }

    // 요청 본문/타입 오류 - 400
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class
    , NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        log.warn("잘못된 요청입니다. message = {}", e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse("요청 형식이 올바르지 않습니다."));
    }

    // 예상 못 한 모든 예외 - 원인은 로그에만 남기고 사용자에게는 일반 메시지만 내려준다
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("처리되지 않은 예외 {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버 오류가 발생했습니다."));
    }

    public record ErrorResponse(String message) {
    }
}
