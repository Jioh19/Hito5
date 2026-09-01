package com.jioh.hito4.infrastructure.web.controller;

import com.jioh.hito4.domain.exception.InvalidUsernameException;
import com.jioh.hito4.domain.exception.UserAlreadyExistsException;
import com.jioh.hito4.domain.exception.UserNotFoundException;
import com.jioh.hito4.infrastructure.web.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidation_returns400_withValidationErrorCode() {
        ResponseEntity<ErrorResponse> response = handler.handleValidation(new InvalidUsernameException("Invalid username: x"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
        assertEquals("Invalid username: x", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleAlreadyExists_returns422_withBusinessRuleViolationCode() {
        ResponseEntity<ErrorResponse> response = handler.handleAlreadyExists(new UserAlreadyExistsException("User already exists with username: jioh"));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("BUSINESS_RULE_VIOLATION", response.getBody().code());
    }

    @Test
    void handleNotFound_returns404_withNotFoundCode() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new UserNotFoundException("User not found with id: 1"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().code());
    }

    @Test
    void handleUnexpected_returns500_withoutLeakingTheOriginalMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(new RuntimeException("column \"pwd\" does not exist at line 42"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }

    @Test
    void handleNoResourceFound_returns404_withGenericMessage() {
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/nonexistent", "html");
        ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().code());
        assertEquals("Resource not found", response.getBody().message());
        assertNotNull(response.getBody().timestamp());
    }
}
