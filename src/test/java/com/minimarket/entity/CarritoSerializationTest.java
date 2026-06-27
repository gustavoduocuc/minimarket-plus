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

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche entera 1L");
        producto.setStock(50);

        Carrito carrito = new Carrito(usuario);
        carrito.setId(1L);
        carrito.agregarProducto(producto, 2);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(carrito);

        assertTrue(json.contains("\"username\":\"cliente\""));
        assertTrue(json.contains("\"nombre\":\"CLIENTE\""));
        assertTrue(json.contains("\"items\":["));
        assertTrue(json.contains("\"cantidad\":2"));
        assertFalse(json.contains("\"usuarios\":["), "JSON must not include Rol.usuarios back-reference");
        assertFalse(json.contains("\"carrito\":{"), "ItemCarrito must not serialize carrito back-reference");
    }
}
