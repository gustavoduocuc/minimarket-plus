package com.minimarket.security;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.minimarket.security.service.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CsrfStatelessTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void catalogoProperties(DynamicPropertyRegistry registry) {
        registry.add("catalogo.base-url", wireMock::baseUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void stubCatalogo() {
        wireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/productos/1"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"id":1,"nombre":"Leche entera 1L","precio":1200.0}
                                """)));
        wireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/api/productos/1/stock"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"productoId":1,"stockDisponible":50}
                                """)));
    }

    @Test
    void authenticatedRequestDoesNotSetSessionCookie() throws Exception {
        String token = JwtTestHelper.tokenFor(jwtTokenService, "admin", "ADMIN");

        mockMvc.perform(get("/api/ventas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    for (Cookie cookie : result.getResponse().getCookies()) {
                        if ("JSESSIONID".equals(cookie.getName())) {
                            throw new AssertionError("JSESSIONID session cookie must not be set");
                        }
                    }
                });
    }

    @Test
    void protectedEndpointRequiresJwtNotSession() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postRequestSucceedsWithoutCsrfTokenWhenJwtIsPresent() throws Exception {
        String token = JwtTestHelper.tokenFor(jwtTokenService, "cliente", "CLIENTE");

        mockMvc.perform(post("/api/carrito")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"producto\":{\"id\":1},\"cantidad\":1}"))
                .andExpect(status().isOk());
    }
}
