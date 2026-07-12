package com.minimarket.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "ProductoCollectionDoc", description = "Colección HAL de productos (CollectionModel)")
public class ProductoCollectionDoc {

    @JsonProperty("_embedded")
    @Schema(description = "Recursos embebidos de la colección")
    private ProductoEmbedded embedded;

    @JsonProperty("_links")
    @Schema(implementation = HalLinks.class)
    private HalLinks links;

    public ProductoEmbedded getEmbedded() {
        return embedded;
    }

    public void setEmbedded(ProductoEmbedded embedded) {
        this.embedded = embedded;
    }

    public HalLinks getLinks() {
        return links;
    }

    public void setLinks(HalLinks links) {
        this.links = links;
    }

    @Schema(name = "ProductoEmbedded")
    public static class ProductoEmbedded {

        @Schema(description = "Lista de productos embebidos")
        private List<ProductoResourceDoc> productoList;

        public List<ProductoResourceDoc> getProductoList() {
            return productoList;
        }

        public void setProductoList(List<ProductoResourceDoc> productoList) {
            this.productoList = productoList;
        }
    }
}
