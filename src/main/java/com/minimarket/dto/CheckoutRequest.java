package com.minimarket.dto;

import com.minimarket.entity.MetodoPago;

public class CheckoutRequest {

    private MetodoPago metodoPago;

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }
}
