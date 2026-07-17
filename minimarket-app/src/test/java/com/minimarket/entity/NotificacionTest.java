package com.minimarket.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificacionTest {

    private Usuario usuario;
    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente");

        notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setMensaje("20% de descuento en abarrotes");
        notificacion.setTipo(TipoNotificacion.PROMOCION);
        notificacion.setLeida(false);
    }

    @Test
    void marcarLeida_dejaNotificacionComoLeida() {
        notificacion.marcarLeida();

        assertTrue(notificacion.isLeida());
    }

    @Test
    void perteneceA_conUsernameDelDestinatario_retornaTrue() {
        assertTrue(notificacion.perteneceA("cliente"));
    }

    @Test
    void perteneceA_conUsernameAjeno_retornaFalse() {
        assertFalse(notificacion.perteneceA("otro"));
    }
}
