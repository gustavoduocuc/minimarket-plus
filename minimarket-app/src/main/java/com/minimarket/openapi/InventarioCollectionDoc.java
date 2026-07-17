package com.minimarket.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "InventarioCollectionDoc", description = "Colección HAL de movimientos de inventario (CollectionModel)")
public class InventarioCollectionDoc {

    @JsonProperty("_embedded")
    private InventarioEmbedded embedded;

    @JsonProperty("_links")
    @Schema(implementation = HalLinks.class)
    private HalLinks links;

    public InventarioEmbedded getEmbedded() {
        return embedded;
    }

    public void setEmbedded(InventarioEmbedded embedded) {
        this.embedded = embedded;
    }

    public HalLinks getLinks() {
        return links;
    }

    public void setLinks(HalLinks links) {
        this.links = links;
    }

    @Schema(name = "InventarioEmbedded")
    public static class InventarioEmbedded {

        private List<InventarioResourceDoc> inventarioList;

        public List<InventarioResourceDoc> getInventarioList() {
            return inventarioList;
        }

        public void setInventarioList(List<InventarioResourceDoc> inventarioList) {
            this.inventarioList = inventarioList;
        }
    }
}
