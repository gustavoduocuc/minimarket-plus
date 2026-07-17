package com.minimarket.service;

import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.MetodoPago;
import com.minimarket.entity.Venta;
import com.minimarket.service.impl.NotificationPaymentProcessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationPaymentProcessorTest {

    private final NotificationPaymentProcessor paymentProcessor = new NotificationPaymentProcessor();

    @Test
    void initiatePaymentDoesNotChangeEstadoPagoWhenAlreadySet() {
        Venta venta = new Venta();
        venta.setMetodoPago(MetodoPago.EFECTIVO);
        venta.setEstadoPago(EstadoPago.PENDIENTE_PAGO);

        assertDoesNotThrow(() -> paymentProcessor.initiatePayment(venta));
        assertEquals(EstadoPago.PENDIENTE_PAGO, venta.getEstadoPago());
    }
}
