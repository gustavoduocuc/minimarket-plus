package com.minimarket.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.minimarket.entity.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ProductoResourceDoc", description = "Producto individual en formato HAL (EntityModel)")
public class ProductoResourceDoc {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Leche entera 1L")
    private String nombre;

    @Schema(example = "1200.0")
    private Double precio;

    @Schema(example = "50", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer stockDisponible;

    private Categoria categoria;

    @JsonProperty("_links")
    @Schema(implementation = HalLinks.class)
    private HalLinks links;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(Integer stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public HalLinks getLinks() {
        return links;
    }

    public void setLinks(HalLinks links) {
        this.links = links;
    }
}
