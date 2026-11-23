package com.felipeaugdev;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import com.felipeaugdev.exception.ResourceNotFoundException;

@SuppressWarnings("null")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void apiExceptionsKeepTheirStatusAndMessage() {
        var response = handler.handleApiException(new ResourceNotFoundException("Budget not found"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("Budget not found", response.getBody().message());
    }

    @Test
    void dataIntegrityViolationsBecomeConflicts() {
        var response = handler.handleDataIntegrity(new DataIntegrityViolationException("duplicate key"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("This change conflicts with existing data", response.getBody().message());
    }
}
