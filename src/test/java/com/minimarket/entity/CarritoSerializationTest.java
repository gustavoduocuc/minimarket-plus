package com.minimarket.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarritoSerializationTest {

    @Test
    void serializingCarritoDoesNotIncludeRolUsuariosBackReference() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(4L);
        usuario.setUsername("cliente");

        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("CLIENTE");
        rol.setUsuarios(Set.of(usuario));
        usuario.setRoles(Set.of(rol));

        Carrito carrito = new Carrito();
        carrito.setId(1L);
        carrito.setCantidad(2);
        carrito.setUsuario(usuario);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(carrito);

        assertTrue(json.contains("\"username\":\"cliente\""));
        assertTrue(json.contains("\"nombre\":\"CLIENTE\""));
        assertFalse(json.contains("\"usuarios\":["), "JSON must not include Rol.usuarios back-reference");
    }
}
