package com.minimarket.dto;

public class AgregarItemCarritoRequest {

    private ProductoRef producto;
    private Integer cantidad;

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

    public static class ProductoRef {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
