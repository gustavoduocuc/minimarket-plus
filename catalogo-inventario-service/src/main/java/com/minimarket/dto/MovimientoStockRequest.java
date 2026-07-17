package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class MovimientoStockRequest {

    @Schema(description = "ID del producto", example = "1")
    private Long productoId;

    @Schema(description = "Cantidad del movimiento", example = "2")
    private Integer cantidad;

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
