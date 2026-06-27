package com.minimarket.dto;

import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.MetodoPago;

public class CheckoutResponse {

    private Long ventaId;
    private MetodoPago metodoPago;
    private EstadoPago estadoPago;
    private double total;

    public CheckoutResponse() {
    }

    public CheckoutResponse(Long ventaId, MetodoPago metodoPago, EstadoPago estadoPago, double total) {
        this.ventaId = ventaId;
        this.metodoPago = metodoPago;
        this.estadoPago = estadoPago;
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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
