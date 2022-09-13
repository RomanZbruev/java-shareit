package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice()
public class ErrorHandler {

    @ExceptionHandler()
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Нет в базе")
    public ErrorResponse handleNotFoundException(NotFoundException exception) {
        return new ErrorResponse(exception.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Неккоректный запрос")
    public ErrorResponse handleValidationException(
            ValidationException exception) {
        return new ErrorResponse(exception.getMessage());
    }


}