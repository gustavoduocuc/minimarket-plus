package com.minimarket.service.impl;

import com.minimarket.entity.Notificacion;
import com.minimarket.entity.TipoNotificacion;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.exception.ForbiddenOperationException;
import com.minimarket.repository.NotificacionRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.NotificacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionServiceImpl(
            NotificacionRepository notificacionRepository,
            UsuarioRepository usuarioRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public Notificacion notificarPromocion(Long destinatarioId, String mensaje) {
        Long destinatarioIdValido = requireNonNull(destinatarioId, "El destinatario es obligatorio");
        validarMensaje(mensaje);
        Usuario destinatario = usuarioRepository.findById(destinatarioIdValido)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario con id " + destinatarioIdValido + " no encontrado"));
        return guardarNotificacion(destinatario, mensaje, TipoNotificacion.PROMOCION);
    }

    @Override
    @Transactional
    public void notificarPromocionATodos(String mensaje) {
        validarMensaje(mensaje);
        for (Usuario usuario : usuarioRepository.findAll()) {
            guardarNotificacion(usuario, mensaje, TipoNotificacion.PROMOCION);
        }
    }

    @Override
    @Transactional
    public void notificarCambioPedido(Venta venta, String mensaje) {
        Venta ventaValida = requireNonNull(venta, "La venta es obligatoria");
        validarMensaje(mensaje);
        Usuario destinatario = requireNonNull(ventaValida.getUsuario(), "Usuario de la venta es obligatorio");
        guardarNotificacion(destinatario, mensaje, TipoNotificacion.CAMBIO_PEDIDO);
    }

    @Override
    public List<Notificacion> listarDe(String username) {
        return notificacionRepository.findByUsuarioUsernameOrderByFechaDesc(username);
    }

    @Override
    @Transactional
    public Notificacion marcarComoLeida(Long id, String username) {
        Long idValido = requireNonNull(id, "El id es obligatorio");
        Notificacion notificacion = notificacionRepository.findById(idValido)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notificacion con id " + idValido + " no encontrada"));
        if (!notificacion.perteneceA(username)) {
            throw new ForbiddenOperationException("No puede marcar notificaciones de otro usuario");
        }
        notificacion.marcarLeida();
        return notificacionRepository.save(notificacion);
    }

    private Notificacion guardarNotificacion(Usuario destinatario, String mensaje, TipoNotificacion tipo) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(destinatario);
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo);
        notificacion.setLeida(false);
        notificacion.setFecha(new Date());
        return notificacionRepository.save(notificacion);
    }

    private void validarMensaje(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("El mensaje es obligatorio");
        }
    }
}
