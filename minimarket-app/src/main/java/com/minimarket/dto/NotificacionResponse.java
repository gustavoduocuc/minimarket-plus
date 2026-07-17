package com.minimarket.dto;

import com.minimarket.entity.TipoNotificacion;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public class NotificacionResponse {

    @Schema(description = "ID de la notificación", example = "1")
    private final Long id;

    @Schema(description = "Mensaje de la notificación", example = "Pedido #5 pagado")
    private final String mensaje;

    @Schema(description = "Tipo de notificación", example = "CAMBIO_PEDIDO")
    private final TipoNotificacion tipo;

    @Schema(description = "Indica si el usuario ya la leyó", example = "false")
    private final boolean leida;

    @Schema(description = "Fecha de creación")
    private final Date fecha;

    public NotificacionResponse(Long id, String mensaje, TipoNotificacion tipo, boolean leida, Date fecha) {
        this.id = id;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.leida = leida;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public String getMensaje() {
        return mensaje;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public boolean isLeida() {
        return leida;
    }

    public Date getFecha() {
        return fecha;
    }
}
