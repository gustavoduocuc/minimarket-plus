package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class StockDisponibleResponse {

    @Schema(description = "ID del producto", example = "1")
    private Long productoId;

    @Schema(description = "Stock disponible calculado desde inventario", example = "50")
    private int stockDisponible;

    public StockDisponibleResponse(Long productoId, int stockDisponible) {
        this.productoId = productoId;
        this.stockDisponible = stockDisponible;
    }

    public Long getProductoId() {
        return productoId;
    }

    public int getStockDisponible() {
        return stockDisponible;
    }
}
