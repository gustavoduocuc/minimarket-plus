package com.minimarket.hateoas;

import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.MetodoPago;
import com.minimarket.entity.TipoEntrega;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
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

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VentaModelAssemblerTest {

    private VentaModelAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new VentaModelAssembler(new HateoasSecurityHelper());
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void toModelIncludesBaseLinksWithoutMutationWhenNotStaff() {
        Venta venta = venta(EstadoPago.PENDIENTE_PAGO);

        EntityModel<Venta> model = assembler.toModel(venta);

        assertTrue(model.hasLink("self"));
        assertTrue(model.hasLink("ventas"));
        assertTrue(model.hasLink("ventasPendientes"));
        assertFalse(model.hasLink("crearVenta"));
        assertFalse(model.hasLink("confirmarPago"));
    }

    @Test
    void toModelAddsCrearAndConfirmarWhenEmpleadoAndPendiente() {
        authenticateAs("ROLE_EMPLEADO");
        Venta venta = venta(EstadoPago.PENDIENTE_PAGO);

        EntityModel<Venta> model = assembler.toModel(venta);

        assertTrue(model.hasLink("crearVenta"));
        assertTrue(model.hasLink("confirmarPago"));
    }

    @Test
    void toModelAddsCrearWithoutConfirmarWhenAlreadyPaid() {
        authenticateAs("ROLE_EMPLEADO");
        Venta venta = venta(EstadoPago.PAGADO);

        EntityModel<Venta> model = assembler.toModel(venta);

        assertTrue(model.hasLink("crearVenta"));
        assertFalse(model.hasLink("confirmarPago"));
    }

    @Test
    void toPendientesCollectionModelAddsCrearOnlyWhenStaff() {
        Venta venta = venta(EstadoPago.PENDIENTE_PAGO);

        CollectionModel<EntityModel<Venta>> withoutPermission =
                assembler.toPendientesCollectionModel(List.of(venta));
        assertTrue(withoutPermission.hasLink("self"));
        assertTrue(withoutPermission.hasLink("ventas"));
        assertTrue(withoutPermission.hasLink("ventasPendientes"));
        assertFalse(withoutPermission.hasLink("crearVenta"));

        authenticateAs("ROLE_GERENTE");
        CollectionModel<EntityModel<Venta>> withPermission =
                assembler.toPendientesCollectionModel(List.of(venta));
        assertTrue(withPermission.hasLink("crearVenta"));
    }

    private static void authenticateAs(String authority) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "usuario",
                        "password",
                        List.of(new SimpleGrantedAuthority(authority))));
    }

    private static Venta venta(EstadoPago estadoPago) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente");

        Venta venta = new Venta();
        venta.setId(10L);
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setMetodoPago(MetodoPago.EFECTIVO);
        venta.setEstadoPago(estadoPago);
        venta.setTipoEntrega(TipoEntrega.RETIRO_EN_TIENDA);
        return venta;
    }
}
