package com.minimarket.service;

import com.minimarket.dto.UsuarioRequestDto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    private static final String encodedPassword = "$2a$10$encodedPasswordHash";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void rejectsCreationWhenPasswordIsMissing() {
        UsuarioRequestDto dto = new UsuarioRequestDto();
        dto.setUsername("nuevo");
        dto.setPassword("");
        dto.setRoles(Set.of("CLIENTE"));

        assertThrows(IllegalArgumentException.class, () -> usuarioService.create(dto));

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void rejectsCreationWhenRoleDoesNotExist() {
        UsuarioRequestDto dto = new UsuarioRequestDto();
        dto.setUsername("nuevo");
        dto.setPassword("Password123!");
        dto.setRoles(Set.of("ROL_INEXISTENTE"));

        when(rolRepository.findByNombre("ROL_INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> usuarioService.create(dto));

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void savesUserWithCompleteMandatoryFieldsWhenCreatingValidUser() {
        UsuarioRequestDto dto = new UsuarioRequestDto();
        dto.setUsername("nuevo");
        dto.setPassword("Password123!");
        dto.setRoles(Set.of("ADMIN"));

        Rol rolAdmin = new Rol();
        rolAdmin.setId(1L);
        rolAdmin.setNombre("ADMIN");

        when(rolRepository.findByNombre("ADMIN")).thenReturn(Optional.of(rolAdmin));
        when(passwordEncoder.encode("Password123!")).thenReturn(encodedPassword);
        whenUsuarioSaved(invocation -> {
            Usuario saved = Objects.requireNonNull(invocation.getArgument(0, Usuario.class));
            saved.setId(1L);
            return saved;
        });

        usuarioService.create(dto);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        Usuario savedUser = verifyUsuarioSaved(usuarioCaptor);
        assertEquals("nuevo", savedUser.getUsername());
        assertEquals(encodedPassword, savedUser.getPassword());
        assertNotNull(savedUser.getRoles());
        assertFalse(savedUser.getRoles().isEmpty());
    }

    @Test
    void assignsDefaultClienteRoleWhenSavingNewUserWithoutRoles() {
        Usuario newUser = new Usuario();
        newUser.setUsername("registrado");
        newUser.setPassword("Password123!");

        Rol rolCliente = new Rol();
        rolCliente.setId(2L);
        rolCliente.setNombre("CLIENTE");

        when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(rolCliente));
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        whenUsuarioSaved(invocation -> Objects.requireNonNull(invocation.getArgument(0, Usuario.class)));

        usuarioService.save(newUser);

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        Usuario savedUser = verifyUsuarioSaved(usuarioCaptor);
        assertEquals("registrado", savedUser.getUsername());
        assertEquals(encodedPassword, savedUser.getPassword());
        assertNotNull(savedUser.getRoles());
        assertEquals(1, savedUser.getRoles().size());
        assertEquals("CLIENTE", savedUser.getRoles().iterator().next().getNombre());
    }

    @SuppressWarnings("null")
    private void whenUsuarioSaved(Answer<Usuario> answer) {
        when(usuarioRepository.save(notNull(Usuario.class))).thenAnswer(answer);
    }

    @SuppressWarnings("null")
    private Usuario verifyUsuarioSaved(ArgumentCaptor<Usuario> captor) {
        verify(usuarioRepository).save(captor.capture());
        return Objects.requireNonNull(captor.getValue());
    }
}
