package com.minimarket.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleInsufficientStockReturnsStructured422Response() {
        InsufficientStockException exception = new InsufficientStockException("Café", 3, 5);

        ResponseEntity<Map<String, Object>> response = handler.handleInsufficientStock(exception);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertAll(
                () -> assertEquals("Stock insuficiente para 'Café'. Solo quedan 3 unidades.", body.get("error")),
                () -> assertEquals("Café", body.get("producto")),
                () -> assertEquals(3, body.get("disponible")),
                () -> assertEquals(5, body.get("solicitado"))
        );
    }

    @Test
    void handleForbiddenOperationReturns403Response() {
        ForbiddenOperationException exception =
                new ForbiddenOperationException("Un cliente solo puede modificar su propio carrito");

        ResponseEntity<Map<String, String>> response = handler.handleForbiddenOperation(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Un cliente solo puede modificar su propio carrito", body.get("error"));
    }
}
