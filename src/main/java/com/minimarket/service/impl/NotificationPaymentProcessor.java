package com.minimarket.service.impl;

import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.Venta;
import com.minimarket.service.PaymentProcessor;
import org.springframework.stereotype.Service;

@Service
public class NotificationPaymentProcessor implements PaymentProcessor {

    @Override
    public void initiatePayment(Venta venta) {
        if (venta.getEstadoPago() == null) {
            venta.setEstadoPago(EstadoPago.PENDIENTE_PAGO);
        }
    }
}
