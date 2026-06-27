package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ProductoMasVendidoResponse {

    @Schema(description = "ID del producto", example = "1")
    private final Long productoId;

    @Schema(description = "Nombre del producto", example = "Arroz 1kg")
    private final String nombre;

    @Schema(description = "Cantidad total vendida en ventas pagadas", example = "42")
    private final int cantidadVendida;

    public ProductoMasVendidoResponse(Long productoId, String nombre, int cantidadVendida) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.cantidadVendida = cantidadVendida;
    }

    public Long getProductoId() {
        return productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidadVendida() {
        return cantidadVendida;
    }
}
