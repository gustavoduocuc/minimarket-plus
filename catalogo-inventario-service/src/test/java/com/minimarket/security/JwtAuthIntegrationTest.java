package com.minimarket.security;

import com.minimarket.security.service.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void returns401JsonOnInvalidBearerToken() throws Exception {
        mockMvc.perform(get("/api/inventario")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token inválido o expirado"));
    }

    @Test
    void allowsPublicProductListing() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    void allowsStockSalidaWithStaffToken() throws Exception {
        String token = JwtTestHelper.tokenFor(jwtTokenService, "empleado", "EMPLEADO");

        mockMvc.perform(post("/api/inventario/salidas")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("{\"productoId\":1,\"cantidad\":1}"))
                .andExpect(status().isNoContent());
    }
}
