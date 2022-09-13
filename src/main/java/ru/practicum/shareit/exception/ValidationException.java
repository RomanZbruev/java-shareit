package ru.practicum.shareit.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public String getMessage() {
        return super.getMessage();
    }
}
