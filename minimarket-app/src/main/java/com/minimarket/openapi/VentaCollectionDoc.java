package com.minimarket.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "VentaCollectionDoc", description = "Colección HAL de ventas (CollectionModel)")
public class VentaCollectionDoc {

    @JsonProperty("_embedded")
    private VentaEmbedded embedded;

    @JsonProperty("_links")
    @Schema(implementation = HalLinks.class)
    private HalLinks links;

    public VentaEmbedded getEmbedded() {
        return embedded;
    }

    public void setEmbedded(VentaEmbedded embedded) {
        this.embedded = embedded;
    }

    public HalLinks getLinks() {
        return links;
    }

    public void setLinks(HalLinks links) {
        this.links = links;
    }

    @Schema(name = "VentaEmbedded")
    public static class VentaEmbedded {

        private List<VentaResourceDoc> ventaList;

        public List<VentaResourceDoc> getVentaList() {
            return ventaList;
        }

        public void setVentaList(List<VentaResourceDoc> ventaList) {
            this.ventaList = ventaList;
        }
    }
}
