package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class PromocionRequest {

    @Schema(description = "ID del usuario destinatario. Si se omite, se envía a todos los usuarios", example = "1")
    private Long destinatarioId;

    @Schema(description = "Mensaje de la promoción", example = "20% de descuento en abarrotes este fin de semana")
    private String mensaje;

    public Long getDestinatarioId() {
        return destinatarioId;
    }

    public void setDestinatarioId(Long destinatarioId) {
        this.destinatarioId = destinatarioId;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
