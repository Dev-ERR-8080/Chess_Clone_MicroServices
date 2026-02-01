package org.example.user_profile_service.expectionHandler;

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
}
