package com.minimarket.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "CarritoCollectionDoc", description = "Colección HAL de carritos (CollectionModel)")
public class CarritoCollectionDoc {

    @JsonProperty("_embedded")
    private CarritoEmbedded embedded;

    @JsonProperty("_links")
    @Schema(implementation = HalLinks.class)
    private HalLinks links;

    public CarritoEmbedded getEmbedded() {
        return embedded;
    }

    public void setEmbedded(CarritoEmbedded embedded) {
        this.embedded = embedded;
    }

    public HalLinks getLinks() {
        return links;
    }

    public void setLinks(HalLinks links) {
        this.links = links;
    }

    @Schema(name = "CarritoEmbedded")
    public static class CarritoEmbedded {

        private List<CarritoResourceDoc> carritoList;

        public List<CarritoResourceDoc> getCarritoList() {
            return carritoList;
        }

        public void setCarritoList(List<CarritoResourceDoc> carritoList) {
            this.carritoList = carritoList;
        }
    }
}
