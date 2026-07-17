package com.minimarket.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarritoSerializationTest {

    @Test
    void serializingCarritoIncludesUsuarioProjectionWithoutBackReferences() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(4L);
        usuario.setUsername("cliente");

        Carrito carrito = new Carrito(usuario);
        carrito.setId(1L);
        carrito.agregarProducto(1L, "Leche entera 1L", 1200.0, 2, 50);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(carrito);

        assertTrue(json.contains("\"username\":\"cliente\""));
        assertTrue(json.contains("\"items\":["));
        assertTrue(json.contains("\"cantidad\":2"));
        assertTrue(json.contains("\"nombreProducto\":\"Leche entera 1L\""));
        assertFalse(json.contains("\"password\""), "JSON must not include password");
        assertFalse(json.contains("\"roles\""), "JSON must not include roles");
        assertFalse(json.contains("\"carrito\":{"), "ItemCarrito must not serialize carrito back-reference");
    }
}
