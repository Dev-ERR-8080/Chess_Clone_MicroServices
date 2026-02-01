package org.example.iam_service.exceptionHandler;

import org.springframework.http.HttpStatus;


public final class ApplicationExceptions {

    private ApplicationExceptions() {
        // Prevent instantiation
    }

    public static abstract class AppException extends RuntimeException {

        private final HttpStatus status;

        protected AppException(String message, HttpStatus status) {
            super(message);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }



    // ===================== AUTH EXCEPTIONS =====================
    public static class Unauthorized extends AppException {
        public Unauthorized(String message) {
            super(message, HttpStatus.UNAUTHORIZED);
        }
    }

    public static class Forbidden extends AppException {
        public Forbidden(String message) {
            super(message, HttpStatus.FORBIDDEN);
        }
    }

    // ===================== USER EXCEPTIONS =====================
    public static class UserNotFound extends AppException {
        public UserNotFound(String message) {
            super(message, HttpStatus.NOT_FOUND);
        }
    }

    public static class EmailAlreadyExists extends AppException {
        public EmailAlreadyExists(String message) {
            super(message, HttpStatus.CONFLICT);
        }
    }

    // ===================== VALIDATION EXCEPTIONS =====================
    public static class BadRequest extends AppException {
        public BadRequest(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }

    // ===================== GAME / BUSINESS EXCEPTIONS =====================
    public static class InvalidMove extends AppException {
        public InvalidMove(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }

    public static class RatingUpdateFailed extends AppException {
        public RatingUpdateFailed(String message) {
            super(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
