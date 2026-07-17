package com.minimarket.dto;

import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.MetodoPago;
import com.minimarket.entity.TipoEntrega;

public class CheckoutResponse {

    private Long ventaId;
    private MetodoPago metodoPago;
    private EstadoPago estadoPago;
    private TipoEntrega tipoEntrega;
    private double total;

    public CheckoutResponse() {
    }

    public CheckoutResponse(
            Long ventaId,
            MetodoPago metodoPago,
            EstadoPago estadoPago,
            TipoEntrega tipoEntrega,
            double total) {
        this.ventaId = ventaId;
        this.metodoPago = metodoPago;
        this.estadoPago = estadoPago;
        this.tipoEntrega = tipoEntrega;
        this.total = total;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public TipoEntrega getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(TipoEntrega tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
