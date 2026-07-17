package com.minimarket.service;

import com.minimarket.entity.Notificacion;
import com.minimarket.entity.TipoNotificacion;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.exception.ForbiddenOperationException;
import com.minimarket.repository.NotificacionRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.NotificacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private NotificacionServiceImpl notificacionService;

    private Usuario cliente;
    private Usuario otroCliente;

    @BeforeEach
    void setUp() {
        cliente = new Usuario();
        cliente.setId(1L);
        cliente.setUsername("cliente");

        otroCliente = new Usuario();
        otroCliente.setId(2L);
        otroCliente.setUsername("otro");
    }

    @Test
    void notificarPromocion_aUsuarioEspecifico_guardaNotificacionConTipoPromocion() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cliente));
        whenSaveAsignaId(10L);

        Notificacion resultado = notificacionService.notificarPromocion(1L, "20% en abarrotes");

        verifyNotificacionGuardada(guardada -> {
            assertEquals(TipoNotificacion.PROMOCION, guardada.getTipo());
            assertFalse(guardada.isLeida());
            assertEquals(cliente, guardada.getUsuario());
            assertEquals("20% en abarrotes", guardada.getMensaje());
        });
        assertEquals(10L, resultado.getId());
    }

    @Test
    void notificarPromocionATodos_creaUnaNotificacionPorCadaUsuario() {
        when(usuarioRepository.findAll()).thenReturn(List.of(cliente, otroCliente));
        whenSaveDevuelvePrimerArgumento();

        notificacionService.notificarPromocionATodos("Promo general");

        verifySaveInvocado(2);
    }

    @Test
    void notificarCambioPedido_guardaNotificacionParaUsuarioDeLaVenta() {
        Venta venta = new Venta();
        venta.setId(5L);
        venta.setUsuario(cliente);

        whenSaveDevuelvePrimerArgumento();

        notificacionService.notificarCambioPedido(venta, "Pedido #5 creado, pendiente de pago");

        verifyNotificacionGuardada(guardada -> {
            assertEquals(TipoNotificacion.CAMBIO_PEDIDO, guardada.getTipo());
            assertEquals(cliente, guardada.getUsuario());
            assertEquals("Pedido #5 creado, pendiente de pago", guardada.getMensaje());
        });
    }

    @Test
    void listarDe_retornaNotificacionesDelUsuario() {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(cliente);
        when(notificacionRepository.findByUsuarioUsernameOrderByFechaDesc("cliente"))
                .thenReturn(List.of(notificacion));

        List<Notificacion> resultado = notificacionService.listarDe("cliente");

        assertEquals(1, resultado.size());
        verify(notificacionRepository).findByUsuarioUsernameOrderByFechaDesc("cliente");
    }

    @Test
    void marcarComoLeida_propia_dejaNotificacionLeida() {
        Notificacion notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setUsuario(cliente);
        notificacion.setLeida(false);

        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));
        when(notificacionRepository.save(requireNonNull(notificacion))).thenReturn(notificacion);

        Notificacion resultado = notificacionService.marcarComoLeida(1L, "cliente");

        assertTrue(resultado.isLeida());
        verify(notificacionRepository).save(requireNonNull(notificacion));
    }

    @Test
    void marcarComoLeida_idInexistente_lanzaIllegalArgumentException() {
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                notificacionService.marcarComoLeida(99L, "cliente"));

        assertEquals("Notificacion con id 99 no encontrada", exception.getMessage());
    }

    @Test
    void marcarComoLeida_notificacionAjena_lanzaForbiddenOperationException() {
        Notificacion notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setUsuario(otroCliente);

        when(notificacionRepository.findById(1L)).thenReturn(Optional.of(notificacion));

        assertThrows(ForbiddenOperationException.class, () ->
                notificacionService.marcarComoLeida(1L, "cliente"));
    }

    @SuppressWarnings("null")
    private void whenSaveAsignaId(long id) {
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation -> {
            Notificacion notificacion = requireNonNull(invocation.getArgument(0, Notificacion.class));
            notificacion.setId(id);
            return notificacion;
        });
    }

    @SuppressWarnings("null")
    private void whenSaveDevuelvePrimerArgumento() {
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocation ->
                requireNonNull(invocation.getArgument(0, Notificacion.class)));
    }

    @SuppressWarnings("null")
    private void verifyNotificacionGuardada(Consumer<Notificacion> assertions) {
        verify(notificacionRepository).save(assertArg(assertions::accept));
    }

    @SuppressWarnings("null")
    private void verifySaveInvocado(int veces) {
        verify(notificacionRepository, times(veces)).save(any(Notificacion.class));
    }
}
