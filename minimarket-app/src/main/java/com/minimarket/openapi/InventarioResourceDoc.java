package com.minimarket.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.minimarket.entity.Producto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(name = "InventarioResourceDoc", description = "Movimiento de inventario en formato HAL (EntityModel)")
public class InventarioResourceDoc {

    @Schema(example = "1")
    private Long id;

    private Producto producto;

    @Schema(example = "10")
    private Integer cantidad;

    @Schema(example = "Entrada")
    private String tipoMovimiento;

    private Date fechaMovimiento;

    @JsonProperty("_links")
    @Schema(implementation = HalLinks.class)
    private HalLinks links;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Date getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(Date fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public HalLinks getLinks() {
        return links;
    }

    public void setLinks(HalLinks links) {
        this.links = links;
    }
}
