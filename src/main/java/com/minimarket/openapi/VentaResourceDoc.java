package com.minimarket.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.MetodoPago;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Schema(name = "VentaResourceDoc", description = "Venta individual en formato HAL (EntityModel)")
public class VentaResourceDoc {

    @Schema(example = "1")
    private Long id;

    @Schema(description = "Usuario de la venta")
    private Map<String, Object> usuario;

    private Date fecha;

    @Schema(description = "Detalles de la venta")
    private List<Map<String, Object>> detalles;

    private MetodoPago metodoPago;

    private EstadoPago estadoPago;

    @JsonProperty("_links")
    @Schema(implementation = HalLinks.class)
    private HalLinks links;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, Object> getUsuario() {
        return usuario;
    }

    public void setUsuario(Map<String, Object> usuario) {
        this.usuario = usuario;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public List<Map<String, Object>> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<Map<String, Object>> detalles) {
        this.detalles = detalles;
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

    public HalLinks getLinks() {
        return links;
    }

    public void setLinks(HalLinks links) {
        this.links = links;
    }
}
