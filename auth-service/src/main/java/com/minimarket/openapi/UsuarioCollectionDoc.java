package com.minimarket.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "UsuarioCollectionDoc",
        description = "Colección HAL de usuarios (CollectionModel). Clave _embedded: usuarioResponseDtoList")
public class UsuarioCollectionDoc {

    @JsonProperty("_embedded")
    private UsuarioEmbedded embedded;

    @JsonProperty("_links")
    @Schema(implementation = HalLinks.class)
    private HalLinks links;

    public UsuarioEmbedded getEmbedded() {
        return embedded;
    }

    public void setEmbedded(UsuarioEmbedded embedded) {
        this.embedded = embedded;
    }

    public HalLinks getLinks() {
        return links;
    }

    public void setLinks(HalLinks links) {
        this.links = links;
    }

    @Schema(name = "UsuarioEmbedded")
    public static class UsuarioEmbedded {

        @Schema(description = "Lista de usuarios (nombre HAL generado desde UsuarioResponseDto)")
        private List<UsuarioResourceDoc> usuarioResponseDtoList;

        public List<UsuarioResourceDoc> getUsuarioResponseDtoList() {
            return usuarioResponseDtoList;
        }

        public void setUsuarioResponseDtoList(List<UsuarioResourceDoc> usuarioResponseDtoList) {
            this.usuarioResponseDtoList = usuarioResponseDtoList;
        }
    }
}
