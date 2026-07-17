package com.minimarket.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "HalLink", description = "Enlace HAL (Hypertext Application Language)")
public class HalLink {

    @Schema(description = "URI del recurso o acción", example = "http://localhost:8080/api/productos/1")
    private String href;

    @Schema(description = "Indica si la URI es templated (RFC 6570)", example = "false")
    private Boolean templated;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Boolean getTemplated() {
        return templated;
    }

    public void setTemplated(Boolean templated) {
        this.templated = templated;
    }
}
