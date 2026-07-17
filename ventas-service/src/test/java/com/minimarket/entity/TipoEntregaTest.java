package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TipoEntregaTest {

    @Test
    void containsRetiroEnTiendaAndDespachoDomicilio() {
        assertEquals(2, TipoEntrega.values().length);
        assertEquals(TipoEntrega.RETIRO_EN_TIENDA, TipoEntrega.valueOf("RETIRO_EN_TIENDA"));
        assertEquals(TipoEntrega.DESPACHO_DOMICILIO, TipoEntrega.valueOf("DESPACHO_DOMICILIO"));
    }
}
