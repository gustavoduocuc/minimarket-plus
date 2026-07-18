package com.minimarket.hateoas;

import com.minimarket.dto.UsuarioResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioModelAssemblerTest {

    private UsuarioModelAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new UsuarioModelAssembler(new HateoasSecurityHelper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toModelIncludesBaseLinksWithoutMutationWhenNotAdmin() {
        UsuarioResponseDto usuario = usuarioResponse();

        EntityModel<UsuarioResponseDto> model = assembler.toModel(usuario);

        assertTrue(model.hasLink("self"));
        assertTrue(model.hasLink("usuarios"));
        assertFalse(model.hasLink("crearUsuario"));
        assertFalse(model.hasLink("actualizarUsuario"));
        assertFalse(model.hasLink("eliminarUsuario"));
    }

    @Test
    void toModelAddsMutationLinksWhenAdmin() {
        authenticateAsAdmin();
        UsuarioResponseDto usuario = usuarioResponse();

        EntityModel<UsuarioResponseDto> model = assembler.toModel(usuario);

        assertTrue(model.hasLink("crearUsuario"));
        assertTrue(model.hasLink("actualizarUsuario"));
        assertTrue(model.hasLink("eliminarUsuario"));
    }

    @Test
    void toCollectionModelAddsCrearLinkOnlyWhenAdmin() {
        UsuarioResponseDto usuario = usuarioResponse();

        CollectionModel<EntityModel<UsuarioResponseDto>> withoutPermission =
                assembler.toCollectionModel(List.of(usuario));
        assertTrue(withoutPermission.hasLink("self"));
        assertTrue(withoutPermission.hasLink("usuarios"));
        assertFalse(withoutPermission.hasLink("crearUsuario"));

        authenticateAsAdmin();
        CollectionModel<EntityModel<UsuarioResponseDto>> withPermission =
                assembler.toCollectionModel(List.of(usuario));
        assertTrue(withPermission.hasLink("crearUsuario"));
    }

    private static void authenticateAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    private static UsuarioResponseDto usuarioResponse() {
        return new UsuarioResponseDto(1L, "cliente", Set.of("CLIENTE"));
    }
}
