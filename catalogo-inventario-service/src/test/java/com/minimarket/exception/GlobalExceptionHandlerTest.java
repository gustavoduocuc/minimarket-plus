package com.minimarket.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void handleIllegalArgumentReturns400Response() {
        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Cantidad inválida"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Cantidad inválida", body.get("error"));
    }

    @Test
    void handleBusinessRuleReturns422Response() {
        ResponseEntity<Map<String, String>> response =
                handler.handleBusinessRule(new IllegalStateException("No se puede confirmar"));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("No se puede confirmar", body.get("error"));
    }

    @Test
    void handleInvalidRequestReturns400WithClientMessage() {
        InvalidRequestException exception = new InvalidRequestException("Datos inválidos", "detalle interno");

        ResponseEntity<Map<String, String>> response = handler.handleInvalidRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Datos inválidos", body.get("error"));
    }

    @Test
    void handleDataIntegrityReturns400WithGenericMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleDataIntegrity(new DataIntegrityViolationException("constraint"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Invalid request", body.get("error"));
    }

    @Test
    void handleGeneralReturns500Response() {
        ResponseEntity<Map<String, String>> response =
                handler.handleGeneral(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Error interno del servidor", body.get("error"));
    }

    @Test
    void handleValidationReturns400WithFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "cantidad", "must be greater than 0")));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("cantidad: must be greater than 0", body.get("error"));
    }
}
