package com.minimarket.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InsufficientStockExceptionTest {

    @Test
    void exposesProductStockDetailsAndClientMessage() {
        InsufficientStockException exception = new InsufficientStockException("Café", 3, 5);

        assertEquals("Café", exception.getProducto());
        assertEquals(3, exception.getDisponible());
        assertEquals(5, exception.getSolicitado());
        assertTrue(exception.getClientMessage().contains("Solo quedan 3 unidades"));
    }
}
