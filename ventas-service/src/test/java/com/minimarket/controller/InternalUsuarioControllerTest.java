package com.minimarket.controller;

import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InternalUsuarioControllerTest {

    private static final String internalToken = "token-interno-local";

    @Mock
    private UsuarioService usuarioService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalUsuarioController controller = new InternalUsuarioController(usuarioService, internalToken);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void ensureUsuario_tokenValido_retorna204() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername("nuevo");
        when(usuarioService.ensure("nuevo")).thenReturn(usuario);

        mockMvc.perform(post("/internal/usuarios")
                        .header("X-Internal-Token", internalToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nuevo\"}"))
                .andExpect(status().isNoContent());

        verify(usuarioService).ensure("nuevo");
    }

    @Test
    void ensureUsuario_tokenInvalido_retorna401() throws Exception {
        mockMvc.perform(post("/internal/usuarios")
                        .header("X-Internal-Token", "token-malo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nuevo\"}"))
                .andExpect(status().isUnauthorized());

        verify(usuarioService, never()).ensure(any());
    }

    @Test
    void ensureUsuario_sinToken_retorna401() throws Exception {
        mockMvc.perform(post("/internal/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nuevo\"}"))
                .andExpect(status().isUnauthorized());

        verify(usuarioService, never()).ensure(any());
    }
}
