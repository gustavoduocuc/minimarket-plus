package com.minimarket.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private Usuario usuario;
    private Rol rolCliente;

    @BeforeEach
    void setUp() {
        rolCliente = new Rol();
        rolCliente.setId(1L);
        rolCliente.setNombre("CLIENTE");

        usuario = new Usuario();
        usuario.setId(4L);
        usuario.setUsername("cliente");
        usuario.setRoles(Set.of(rolCliente));
    }

    @Test
    void tieneRol_devuelveTrueCuandoUsuarioTieneElRol() {
        assertTrue(usuario.tieneRol("CLIENTE"));
    }

    @Test
    void tieneRol_devuelveFalseCuandoUsuarioNoTieneElRol() {
        assertFalse(usuario.tieneRol("ADMIN"));
    }
}
