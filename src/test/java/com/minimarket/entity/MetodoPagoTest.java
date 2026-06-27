package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetodoPagoTest {

    @Test
    void containsEfectivoDebitoAndCredito() {
        assertEquals(3, MetodoPago.values().length);
        assertEquals(MetodoPago.EFECTIVO, MetodoPago.valueOf("EFECTIVO"));
        assertEquals(MetodoPago.DEBITO, MetodoPago.valueOf("DEBITO"));
        assertEquals(MetodoPago.CREDITO, MetodoPago.valueOf("CREDITO"));
    }
}
