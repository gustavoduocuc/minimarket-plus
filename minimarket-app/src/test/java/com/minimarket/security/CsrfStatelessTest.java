package com.minimarket.security;

import com.minimarket.security.service.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CsrfStatelessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

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
