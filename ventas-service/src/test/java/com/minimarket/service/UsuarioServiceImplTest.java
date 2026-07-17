package com.minimarket.service;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void ensure_usuarioExistente_retornaSinCrear() {
        Usuario existente = new Usuario();
        existente.setId(4L);
        existente.setUsername("cliente");
        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.of(existente));

        Usuario resultado = usuarioService.ensure("cliente");

        assertEquals(4L, resultado.getId());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void ensure_usuarioInexistente_creaProyeccion() {
        when(usuarioRepository.findByUsername("nuevo")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(10L);
            return usuario;
        });

        Usuario resultado = usuarioService.ensure("nuevo");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("nuevo", captor.getValue().getUsername());
        assertEquals(10L, resultado.getId());
    }

    @Test
    void ensure_usernameNuloOBlank_lanzaIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> usuarioService.ensure(null));
        assertThrows(IllegalArgumentException.class, () -> usuarioService.ensure("  "));
        verify(usuarioRepository, never()).save(any());
    }
}
