package com.minimarket.service;

import com.minimarket.entity.Venta;

public interface PaymentProcessor {
    void initiatePayment(Venta venta);
}
