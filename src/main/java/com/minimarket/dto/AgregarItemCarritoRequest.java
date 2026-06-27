package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AgregarItemCarritoRequest {

    @Schema(description = "Usuario destino (opcional; solo staff puede indicar otro usuario)")
    private UsuarioRef usuario;

    @Schema(description = "Producto a agregar")
    private ProductoRef producto;

    @Schema(description = "Cantidad a agregar", example = "2")
    private Integer cantidad;

    public UsuarioRef getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioRef usuario) {
        this.usuario = usuario;
    }

    public ProductoRef getProducto() {
        return producto;
    }

    public void setProducto(ProductoRef producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    @Schema(name = "UsuarioRef")
    public static class UsuarioRef {
        @Schema(description = "ID del usuario", example = "4")
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    @Schema(name = "ProductoRef")
    public static class ProductoRef {
        @Schema(description = "ID del producto", example = "1")
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
