package com.minimarket.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleInsufficientStockReturnsStructured422Response() {
        InsufficientStockException exception = new InsufficientStockException("Café", 3, 5);

        ResponseEntity<Map<String, Object>> response = handler.handleInsufficientStock(exception);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Stock insuficiente para 'Café'. Solo quedan 3 unidades.", response.getBody().get("error"));
        assertEquals("Café", response.getBody().get("producto"));
        assertEquals(3, response.getBody().get("disponible"));
        assertEquals(5, response.getBody().get("solicitado"));
    }
}
