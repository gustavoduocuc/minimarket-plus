package com.minimarket.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(name = "CarritoResourceDoc", description = "Carrito individual en formato HAL (EntityModel)")
public class CarritoResourceDoc {

    @Schema(example = "1")
    private Long id;

    @Schema(description = "Usuario dueño del carrito")
    private Map<String, Object> usuario;

    @Schema(description = "Ítems del carrito")
    private List<Map<String, Object>> items;

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

    public List<Map<String, Object>> getItems() {
        return items;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
    }

    public HalLinks getLinks() {
        return links;
    }

    public void setLinks(HalLinks links) {
        this.links = links;
    }
}
