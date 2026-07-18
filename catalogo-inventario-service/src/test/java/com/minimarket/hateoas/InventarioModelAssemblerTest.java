package com.minimarket.hateoas;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.inventario.TipoMovimientoInventario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventarioModelAssemblerTest {

    private InventarioModelAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new InventarioModelAssembler(new HateoasSecurityHelper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toModelIncludesBaseAndProductoLinksWithoutMutationWhenNotManager() {
        Inventario inventario = movimientoConProducto();

        EntityModel<Inventario> model = assembler.toModel(inventario);

        assertTrue(model.hasLink("self"));
        assertTrue(model.hasLink("inventario"));
        assertTrue(model.hasLink("producto"));
        assertFalse(model.hasLink("registrarMovimiento"));
        assertFalse(model.hasLink("actualizarMovimiento"));
        assertFalse(model.hasLink("eliminarMovimiento"));
    }

    @Test
    void toModelAddsMutationLinksWhenGerente() {
        authenticateAs("ROLE_GERENTE");
        Inventario inventario = movimientoConProducto();

        EntityModel<Inventario> model = assembler.toModel(inventario);

        assertTrue(model.getLink("registrarMovimiento").map(Link::getHref).isPresent());
        assertTrue(model.hasLink("actualizarMovimiento"));
        assertTrue(model.hasLink("eliminarMovimiento"));
    }

    @Test
    void toCollectionModelAddsRegistrarLinkOnlyWhenManager() {
        Inventario inventario = movimientoConProducto();

        CollectionModel<EntityModel<Inventario>> withoutPermission =
                assembler.toCollectionModel(List.of(inventario));
        assertTrue(withoutPermission.hasLink("self"));
        assertTrue(withoutPermission.hasLink("inventario"));
        assertFalse(withoutPermission.hasLink("registrarMovimiento"));

        authenticateAs("ROLE_ADMIN");
        CollectionModel<EntityModel<Inventario>> withPermission =
                assembler.toCollectionModel(List.of(inventario));
        assertTrue(withPermission.hasLink("registrarMovimiento"));
    }

    private static void authenticateAs(String authority) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "usuario",
                        "password",
                        List.of(new SimpleGrantedAuthority(authority))));
    }

    private static Inventario movimientoConProducto() {
        Producto producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Café");

        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(5);
        inventario.setTipoMovimiento(TipoMovimientoInventario.entrada);
        inventario.setFechaMovimiento(new Date());
        return inventario;
    }
}
