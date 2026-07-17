package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.dto.PromocionRequest;
import com.minimarket.entity.Notificacion;
import com.minimarket.entity.TipoNotificacion;
import com.minimarket.entity.Usuario;
import com.minimarket.service.NotificacionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private NotificacionController notificacionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Notificacion notificacion;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificacionController).build();
        objectMapper = new ObjectMapper();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("cliente", null));

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente");

        notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setUsuario(usuario);
        notificacion.setMensaje("Pedido #5 pagado");
        notificacion.setTipo(TipoNotificacion.CAMBIO_PEDIDO);
        notificacion.setLeida(false);
        notificacion.setFecha(new Date());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void enviarPromocion_conDestinatario_delegaANotificarPromocion() throws Exception {
        PromocionRequest request = new PromocionRequest();
        request.setDestinatarioId(1L);
        request.setMensaje("20% en abarrotes");

        when(notificacionService.notificarPromocion(1L, "20% en abarrotes")).thenReturn(notificacion);

        mockMvc.perform(post("/api/notificaciones/promociones")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(notificacionService).notificarPromocion(1L, "20% en abarrotes");
    }

    @Test
    void enviarPromocion_sinDestinatario_delegaANotificarPromocionATodos() throws Exception {
        PromocionRequest request = new PromocionRequest();
        request.setMensaje("Promo general");

        mockMvc.perform(post("/api/notificaciones/promociones")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isCreated());

        verify(notificacionService).notificarPromocionATodos("Promo general");
    }

    @Test
    void listarNotificaciones_retorna200ConListaDelUsuario() throws Exception {
        when(notificacionService.listarDe("cliente")).thenReturn(List.of(notificacion));

        mockMvc.perform(get("/api/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].mensaje").value("Pedido #5 pagado"))
                .andExpect(jsonPath("$[0].tipo").value("CAMBIO_PEDIDO"));

        verify(notificacionService).listarDe("cliente");
    }

    @Test
    void marcarComoLeida_retorna200() throws Exception {
        notificacion.setLeida(true);
        when(notificacionService.marcarComoLeida(eq(1L), eq("cliente"))).thenReturn(notificacion);

        mockMvc.perform(patch("/api/notificaciones/1/leida"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leida").value(true));

        verify(notificacionService).marcarComoLeida(1L, "cliente");
    }
}
