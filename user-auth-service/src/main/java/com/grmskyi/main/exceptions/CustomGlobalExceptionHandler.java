package com.grmskyi.main.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomGlobalExceptionHandler {

    /**
     * Handles {@link MethodArgumentNotValidException}, which occurs when method arguments
     * annotated with validation constraints fail to validate.
     *
     * @param ex The exception that was thrown when validation failed.
     * @return A {@link ResponseEntity} containing an {@link ApiError} object detailing the validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation error", errors);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles {@link ResponseStatusException}, typically used when a handler method
     * wants to return a response status with a message but without throwing an exception.
     *
     * @param ex The exception representing the response status and message.
     * @return A {@link ResponseEntity} containing an {@link ApiError} object with the error details.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(ResponseStatusException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                ex.getReason(),
                new ArrayList<>()
        );
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }
}