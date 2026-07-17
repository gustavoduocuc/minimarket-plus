package com.minimarket.service;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.MetodoPago;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.exception.CatalogoServiceUnavailableException;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CarritoCheckoutIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void catalogoProperties(DynamicPropertyRegistry registry) {
        registry.add("catalogo.base-url", wireMock::baseUrl);
    }

    @Autowired
    private CarritoCheckoutService carritoCheckoutService;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @BeforeEach
    void setUp() {
        wireMock.resetAll();
        Usuario cliente = usuarioRepository.findByUsername("cliente").orElseThrow();
        carritoRepository.findByUsuarioId(cliente.getId()).ifPresent(carritoRepository::delete);

        wireMock.stubFor(get(urlEqualTo("/api/productos/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"id":1,"nombre":"Leche entera 1L","precio":1200.0}
                                """)));
        wireMock.stubFor(get(urlEqualTo("/api/productos/1/stock"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"productoId":1,"stockDisponible":50}
                                """)));
    }

    @Test
    void checkoutWithWireMockCatalogCompletesWithoutError() {
        wireMock.stubFor(post(urlEqualTo("/api/inventario/salidas"))
                .willReturn(aResponse().withStatus(204)));

        carritoService.agregarProducto("cliente", null, 1L, 1);

        Venta venta = assertDoesNotThrow(() ->
                carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO));

        assertNotNull(venta.getId());
        assertEquals(EstadoPago.PENDIENTE_PAGO, venta.getEstadoPago());
        assertEquals(1L, venta.getDetalles().get(0).getProductoId());
        assertEquals("Leche entera 1L", venta.getDetalles().get(0).getNombreProducto());
    }

    @Test
    void checkoutCuandoCatalogoCaidoEnSalida_noDejaVentaPersistidaComoPagada() {
        wireMock.stubFor(post(urlEqualTo("/api/inventario/salidas"))
                .willReturn(aResponse().withStatus(503)));

        carritoService.agregarProducto("cliente", null, 1L, 1);
        long ventasAntes = ventaRepository.count();

        assertThrows(CatalogoServiceUnavailableException.class, () ->
                carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO));

        assertEquals(ventasAntes, ventaRepository.count());
        assertTrue(ventaRepository.findAll().stream()
                .noneMatch(v -> v.getEstadoPago() == EstadoPago.PAGADO
                        && v.getUsuario() != null
                        && "cliente".equals(v.getUsuario().getUsername())));
    }
}
