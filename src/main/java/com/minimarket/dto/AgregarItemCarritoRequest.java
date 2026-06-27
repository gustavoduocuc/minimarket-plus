package com.minimarket.dto;

public class AgregarItemCarritoRequest {

    private UsuarioRef usuario;
    private ProductoRef producto;
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

    public static class UsuarioRef {
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
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
