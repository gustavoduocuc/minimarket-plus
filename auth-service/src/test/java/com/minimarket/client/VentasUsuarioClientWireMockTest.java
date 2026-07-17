package com.minimarket.client;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class VentasUsuarioClientWireMockTest {

    private static final String internalToken = "token-interno-local";

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private VentasUsuarioClient ventasUsuarioClient;

    @BeforeEach
    void setUp() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMillis(500))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(500));
        RestClient restClient = RestClient.builder()
                .baseUrl(wireMock.baseUrl())
                .requestFactory(requestFactory)
                .build();
        ventasUsuarioClient = new VentasUsuarioClient(restClient, internalToken);
    }

    @Test
    void ensureUsuario_ventasOk_enviaPostConToken() {
        wireMock.stubFor(post(urlEqualTo("/internal/usuarios"))
                .willReturn(aResponse().withStatus(204)));

        ventasUsuarioClient.ensureUsuario("nuevoCliente");

        wireMock.verify(postRequestedFor(urlEqualTo("/internal/usuarios"))
                .withHeader("X-Internal-Token", equalTo(internalToken))
                .withRequestBody(containing("\"username\":\"nuevoCliente\"")));
    }

    @Test
    void ensureUsuario_ventasCaido_noLanzaExcepcion() {
        wireMock.stubFor(post(urlEqualTo("/internal/usuarios"))
                .willReturn(aResponse().withStatus(503)));

        assertDoesNotThrow(() -> ventasUsuarioClient.ensureUsuario("nuevoCliente"));
    }
}
