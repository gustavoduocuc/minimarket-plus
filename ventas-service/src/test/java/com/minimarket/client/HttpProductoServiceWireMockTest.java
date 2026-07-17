package com.minimarket.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.minimarket.catalogo.Producto;
import com.minimarket.exception.CatalogoServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

class HttpProductoServiceWireMockTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private HttpProductoService productoService;

    @BeforeEach
    void setUp() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(1000))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(1000));
        RestClient restClient = RestClient.builder()
                .baseUrl(wireMock.baseUrl())
                .requestFactory(requestFactory)
                .build();
        productoService = new HttpProductoService(restClient);
    }

    @Test
    void findById_ok_retornaProducto() {
        wireMock.stubFor(get(urlEqualTo("/api/productos/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"id":1,"nombre":"Leche entera 1L","precio":1200.0}
                                """)));

        Producto producto = productoService.findById(1L);

        assertNotNull(producto);
        assertEquals(1L, producto.getId());
        assertEquals("Leche entera 1L", producto.getNombre());
        assertEquals(1200.0, producto.getPrecio());
    }

    @Test
    void findById_404_retornaNull() {
        wireMock.stubFor(get(urlEqualTo("/api/productos/99"))
                .willReturn(aResponse().withStatus(404)));

        assertNull(productoService.findById(99L));
    }

    @Test
    void consultarStock_ok_retornaCantidad() {
        wireMock.stubFor(get(urlEqualTo("/api/productos/1/stock"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"productoId":1,"stockDisponible":42}
                                """)));

        assertEquals(42, productoService.consultarStock(1L));
    }

    @Test
    void consultarStock_servicioCaido_lanzaCatalogoServiceUnavailableException() {
        wireMock.stubFor(get(urlEqualTo("/api/productos/1/stock"))
                .willReturn(aResponse().withStatus(503)));

        assertThrows(CatalogoServiceUnavailableException.class, () ->
                productoService.consultarStock(1L));
    }
}
