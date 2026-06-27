package com.minimarket.dto;

import com.minimarket.entity.MetodoPago;
import io.swagger.v3.oas.annotations.media.Schema;

public class CheckoutRequest {

    @Schema(description = "Método de pago de la venta", example = "EFECTIVO")
    private MetodoPago metodoPago;

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }
}
