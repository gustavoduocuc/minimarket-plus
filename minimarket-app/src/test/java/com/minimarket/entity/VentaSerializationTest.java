package com.minimarket.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VentaSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesVentaWithoutCircularReferenceInDetalles() throws Exception {
        Venta venta = new Venta();
        venta.setId(1L);
        venta.setEstadoPago(EstadoPago.PENDIENTE_PAGO);
        venta.setMetodoPago(MetodoPago.EFECTIVO);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(1L);
        detalle.setVenta(venta);
        detalle.setCantidad(2);
        detalle.setPrecio(100.0);
        venta.setDetalles(List.of(detalle));

        String json = objectMapper.writeValueAsString(venta);

        assertFalse(json.contains("\"venta\":{"),
                "DetalleVenta no debe serializar la referencia inversa a Venta");
        assertTrue(json.length() < 500,
                "JSON no debe crecer por recursión circular, longitud=" + json.length());
    }
}
