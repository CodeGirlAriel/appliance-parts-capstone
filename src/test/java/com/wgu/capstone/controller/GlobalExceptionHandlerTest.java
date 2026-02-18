package com.wgu.capstone.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleIllegalArgumentException() {
        // Given
        String errorMessage = "Search query cannot be empty";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // When
        ResponseEntity<Map<String, String>> response = 
            exceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().get("message"));
        assertEquals("400", response.getBody().get("status"));
    }

    @Test
    void testHandleIllegalArgumentException_WithNullMessage() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException();

        // When
        ResponseEntity<Map<String, String>> response = 
            exceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("message"));
    }

    @Test
    void testHandleGenericException() {
        // Given
        String errorMessage = "Database connection failed";
        Exception exception = new RuntimeException(errorMessage);

        // When
        ResponseEntity<Map<String, String>> response = 
            exceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains(errorMessage));
        assertEquals("500", response.getBody().get("status"));
    }

    @Test
    void testHandleGenericException_WithNullMessage() {
        // Given
        Exception exception = new RuntimeException();

        // When
        ResponseEntity<Map<String, String>> response = 
            exceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("message"));
        assertEquals("500", response.getBody().get("status"));
    }

    @Test
    void testHandleGenericException_WithNestedException() {
        // Given
        Exception nestedException = new IllegalArgumentException("Nested error");
        Exception exception = new RuntimeException("Outer error", nestedException);

        // When
        ResponseEntity<Map<String, String>> response = 
            exceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("Outer error"));
    }

    @Test
    void testHandleGenericException_WithIllegalStateException() {
        // Given
        IllegalStateException exception = new IllegalStateException("Not enough stock");

        // When
        ResponseEntity<Map<String, String>> response = 
            exceptionHandler.handleGenericException(exception);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("message").contains("Not enough stock"));
        assertEquals("500", response.getBody().get("status"));
    }
}

