package com.minimarket.service;

import com.minimarket.entity.Notificacion;
import com.minimarket.entity.Venta;

import java.util.List;

public interface NotificacionService {

    Notificacion notificarPromocion(Long destinatarioId, String mensaje);

    void notificarPromocionATodos(String mensaje);

    void notificarCambioPedido(Venta venta, String mensaje);

    List<Notificacion> listarDe(String username);

    Notificacion marcarComoLeida(Long id, String username);
}
