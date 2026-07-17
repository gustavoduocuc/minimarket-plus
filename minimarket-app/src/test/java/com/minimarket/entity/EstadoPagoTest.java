package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EstadoPagoTest {

    @Test
    void containsPendientePagoAndPagado() {
        assertEquals(2, EstadoPago.values().length);
        assertEquals(EstadoPago.PENDIENTE_PAGO, EstadoPago.valueOf("PENDIENTE_PAGO"));
        assertEquals(EstadoPago.PAGADO, EstadoPago.valueOf("PAGADO"));
    }
}
