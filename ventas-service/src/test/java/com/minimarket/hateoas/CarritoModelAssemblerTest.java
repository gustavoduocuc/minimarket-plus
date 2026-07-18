package com.minimarket.hateoas;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarritoModelAssemblerTest {

    private CarritoModelAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new CarritoModelAssembler(new HateoasSecurityHelper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toModelPorIdIncludesBaseLinksWithoutMutationWhenNotOperator() {
        Carrito carrito = carrito();

        EntityModel<Carrito> model = assembler.toModelPorId(carrito);

        assertTrue(model.hasLink("self"));
        assertTrue(model.hasLink("carrito"));
        assertFalse(model.hasLink("agregarProducto"));
    }

    @Test
    void toModelPorIdAddsAgregarProductoWhenCliente() {
        authenticateAs("ROLE_CLIENTE");
        Carrito carrito = carrito();

        EntityModel<Carrito> model = assembler.toModelPorId(carrito);

        assertTrue(model.hasLink("self"));
        assertTrue(model.hasLink("carrito"));
        assertTrue(model.hasLink("agregarProducto"));
    }

    private static void authenticateAs(String authority) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "cliente",
                        "password",
                        List.of(new SimpleGrantedAuthority(authority))));
    }

    private static Carrito carrito() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente");

        Carrito carrito = new Carrito(usuario);
        carrito.setId(5L);
        return carrito;
    }
}
