package com.minimarket.controller;

import com.minimarket.client.VentasUsuarioClient;
import com.minimarket.entity.Usuario;
import com.minimarket.security.model.RegistroRequest;
import com.minimarket.security.service.JwtTokenService;
import com.minimarket.security.service.LoginAttemptService;
import com.minimarket.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerRegistroSyncTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private UsuarioService usuarioService;
    @Mock
    private LoginAttemptService loginAttemptService;
    @Mock
    private VentasUsuarioClient ventasUsuarioClient;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(
                authenticationManager,
                userDetailsService,
                jwtTokenService,
                usuarioService,
                loginAttemptService,
                ventasUsuarioClient);
    }

    @Test
    void registrarUsuario_guardaYSincronizaConVentas() {
        RegistroRequest request = new RegistroRequest();
        request.setUsername("nuevoCliente");
        request.setPassword("Cliente123!");
        when(usuarioService.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Map<String, String>> response = authController.registrarUsuario(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioService).save(usuarioCaptor.capture());
        assertEquals("nuevoCliente", usuarioCaptor.getValue().getUsername());
        verify(ventasUsuarioClient).ensureUsuario("nuevoCliente");
    }
}
