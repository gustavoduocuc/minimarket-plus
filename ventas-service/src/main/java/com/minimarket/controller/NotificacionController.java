package com.minimarket.controller;

import com.minimarket.dto.NotificacionResponse;
import com.minimarket.dto.PromocionRequest;
import com.minimarket.entity.Notificacion;
import com.minimarket.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notificaciones", description = "Notificaciones personalizadas de promociones y cambios de pedido")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @Operation(
            summary = "Enviar promoción",
            description = """
                    Roles: GERENTE, ADMIN.
                    Envía una notificación de promoción a un usuario específico o a todos si se omite destinatarioId.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Promoción enviada",
                    content = @Content(schema = @Schema(implementation = NotificacionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping("/promociones")
    public ResponseEntity<NotificacionResponse> enviarPromocion(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "Promoción a un usuario",
                                    value = "{\"destinatarioId\":1,\"mensaje\":\"20% de descuento en abarrotes\"}"),
                            @ExampleObject(
                                    name = "Promoción a todos",
                                    value = "{\"mensaje\":\"Promo general de fin de semana\"}")
                    }))
            @RequestBody PromocionRequest request) {
        if (request.getDestinatarioId() == null) {
            notificacionService.notificarPromocionATodos(request.getMensaje());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        Notificacion notificacion = notificacionService.notificarPromocion(
                request.getDestinatarioId(), request.getMensaje());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(notificacion));
    }

    @Operation(
            summary = "Listar mis notificaciones",
            description = "Autenticado. Devuelve las notificaciones del usuario del token, más recientes primero.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de notificaciones",
                    content = @Content(schema = @Schema(implementation = NotificacionResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping
    public List<NotificacionResponse> listarNotificaciones() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return notificacionService.listarDe(username).stream()
                .map(this::toResponse)
                .toList();
    }

    @Operation(
            summary = "Marcar notificación como leída",
            description = "Autenticado. Solo puede marcar notificaciones propias.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notificación marcada como leída",
                    content = @Content(schema = @Schema(implementation = NotificacionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Notificación no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Notificación de otro usuario")
    })
    @PatchMapping("/{id}/leida")
    public NotificacionResponse marcarComoLeida(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Notificacion notificacion = notificacionService.marcarComoLeida(id, username);
        return toResponse(notificacion);
    }

    private NotificacionResponse toResponse(Notificacion notificacion) {
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getMensaje(),
                notificacion.getTipo(),
                notificacion.isLeida(),
                notificacion.getFecha());
    }
}
