package com.example.delivery.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 상태코드를 가진 예외. 서비스/엔티티에서 던지면 GlobalExceptionHandler가 해당 상태코드로 응답한다.
 * 사용: throw new GlobalException.NotFound("존재하지 않는 주문입니다.");
 */
@Getter
public class GlobalException extends RuntimeException {

    private final HttpStatus status;

    public GlobalException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    // 400
    public static class BadRequest extends GlobalException {
        public BadRequest(String message) {
            super(HttpStatus.BAD_REQUEST, message);
        }
    }

    // 401
    public static class Unauthorized extends GlobalException {
        public Unauthorized(String message) {
            super(HttpStatus.UNAUTHORIZED, message);
        }
    }

    // 403
    public static class Forbidden extends GlobalException {
        public Forbidden(String message) {
            super(HttpStatus.FORBIDDEN, message);
        }
    }

    // 404
    public static class NotFound extends GlobalException {
        public NotFound(String message) {
            super(HttpStatus.NOT_FOUND, message);
        }
    }

    // 409
    public static class Conflict extends GlobalException {
        public Conflict(String message) {
            super(HttpStatus.CONFLICT, message);
        }
    }
}
