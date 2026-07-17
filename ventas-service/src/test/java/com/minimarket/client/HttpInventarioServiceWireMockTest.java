package com.minimarket.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.minimarket.exception.CatalogoServiceUnavailableException;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.service.ProductoService;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpInventarioServiceWireMockTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private HttpInventarioService inventarioService;
    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        productoService = mock(ProductoService.class);
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
        inventarioService = new HttpInventarioService(
                restClient,
                new BearerTokenPropagator(),
                productoService,
                new ObjectMapper());
    }

    @Test
    void consultarStockDisponible_delegaAProductoService() {
        when(productoService.consultarStock(1L)).thenReturn(15);
        assertEquals(15, inventarioService.consultarStockDisponible(1L));
    }

    @Test
    void registrarSalida_ok_noLanza() {
        wireMock.stubFor(post(urlEqualTo("/api/inventario/salidas"))
                .willReturn(aResponse().withStatus(204)));

        assertDoesNotThrow(() -> inventarioService.registrarSalida(1L, 2));

        wireMock.verify(postRequestedFor(urlEqualTo("/api/inventario/salidas"))
                .withRequestBody(containing("\"productoId\":1"))
                .withRequestBody(containing("\"cantidad\":2")));
    }

    @Test
    void registrarSalida_stockInsuficiente_lanzaInsufficientStockException() {
        wireMock.stubFor(post(urlEqualTo("/api/inventario/salidas"))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"error":"Stock insuficiente","producto":"Cafe","disponible":1,"solicitado":5}
                                """)));

        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () ->
                inventarioService.registrarSalida(1L, 5));

        assertEquals("Cafe", exception.getProducto());
        assertEquals(1, exception.getDisponible());
        assertEquals(5, exception.getSolicitado());
    }

    @Test
    void registrarSalida_servicioCaido_lanzaCatalogoServiceUnavailableException() {
        wireMock.stubFor(post(urlEqualTo("/api/inventario/salidas"))
                .willReturn(aResponse().withStatus(503)));

        assertThrows(CatalogoServiceUnavailableException.class, () ->
                inventarioService.registrarSalida(1L, 2));
    }
}
