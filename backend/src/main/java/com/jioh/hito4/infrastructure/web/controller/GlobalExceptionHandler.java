package com.jioh.hito4.infrastructure.web.controller;

import com.jioh.hito4.domain.exception.InvalidEmailException;
import com.jioh.hito4.domain.exception.InvalidUsernameException;
import com.jioh.hito4.domain.exception.UserAlreadyExistsException;
import com.jioh.hito4.domain.exception.UserNotFoundException;
import com.jioh.hito4.infrastructure.web.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({InvalidEmailException.class, InvalidUsernameException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleValidation(RuntimeException ex) {
        return build(ex.getMessage(), "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExists(UserAlreadyExistsException ex) {
        return build(ex.getMessage(), "BUSINESS_RULE_VIOLATION", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
        return build(ex.getMessage(), "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        return build("Resource not found", "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build("Invalid parameter format", "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        return build("Malformed request body", "VALIDATION_ERROR", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build("HTTP method not supported for this endpoint", "METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return build("Unsupported content type", "UNSUPPORTED_MEDIA_TYPE", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return build("An unexpected error occurred", "INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> build(String message, String code, HttpStatus status) {
        return new ResponseEntity<>(new ErrorResponse(message, code, LocalDateTime.now()), status);
    }
}
