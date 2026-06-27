package com.minimarket.dto;

import com.minimarket.entity.EstadoPago;

public class ConfirmarPagoResponse {

    private Long ventaId;
    private EstadoPago estadoPago;

    public ConfirmarPagoResponse() {
    }

    public ConfirmarPagoResponse(Long ventaId, EstadoPago estadoPago) {
        this.ventaId = ventaId;
        this.estadoPago = estadoPago;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }
}
