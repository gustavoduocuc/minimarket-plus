package com.minimarket.dto;

import com.minimarket.entity.MetodoPago;
import com.minimarket.entity.TipoEntrega;
import io.swagger.v3.oas.annotations.media.Schema;

public class CheckoutRequest {

    @Schema(description = "Método de pago de la venta", example = "EFECTIVO")
    private MetodoPago metodoPago;

    @Schema(description = "Tipo de entrega (opcional; default RETIRO_EN_TIENDA)", example = "DESPACHO_DOMICILIO")
    private TipoEntrega tipoEntrega;

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public TipoEntrega getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(TipoEntrega tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }
}
