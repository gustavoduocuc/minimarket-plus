package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VentaTest {

    @Test
    void calculateTotalReturnsZeroWhenNoDetails() {
        Venta venta = new Venta();

        assertEquals(0.0, venta.calculateTotal());
    }

    @Test
    void calculateTotalReturnsZeroWhenDetailsIsEmpty() {
        Venta venta = new Venta();
        venta.setDetalles(Collections.emptyList());

        assertEquals(0.0, venta.calculateTotal());
    }

    @Test
    void calculateTotalSumsSingleDetailCorrectly() {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setCantidad(2);
        detalle.setPrecio(100.0);

        Venta venta = new Venta();
        venta.setDetalles(List.of(detalle));

        assertEquals(200.0, venta.calculateTotal());
    }

    @Test
    void calculateTotalSumsMultipleDetailsCorrectly() {
        DetalleVenta detalle1 = new DetalleVenta();
        detalle1.setCantidad(2);
        detalle1.setPrecio(100.0);

        DetalleVenta detalle2 = new DetalleVenta();
        detalle2.setCantidad(3);
        detalle2.setPrecio(50.0);

        Venta venta = new Venta();
        venta.setDetalles(Arrays.asList(detalle1, detalle2));

        assertEquals(350.0, venta.calculateTotal());
    }

    @Test
    void storesMetodoPagoAndEstadoPago() {
        Venta venta = new Venta();
        venta.setMetodoPago(MetodoPago.DEBITO);
        venta.setEstadoPago(EstadoPago.PENDIENTE_PAGO);

        assertEquals(MetodoPago.DEBITO, venta.getMetodoPago());
        assertEquals(EstadoPago.PENDIENTE_PAGO, venta.getEstadoPago());
    }
}
