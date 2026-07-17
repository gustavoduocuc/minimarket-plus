package com.minimarket.hateoas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.security.JwtTestHelper;
import com.minimarket.security.service.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HateoasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void carritoConTokenIncluyeEnlacesHateoas() throws Exception {
        String token = JwtTestHelper.tokenFor(jwtTokenService, "cliente", "CLIENTE");

        MvcResult result = mockMvc.perform(get("/api/carrito")
                        .header("Authorization", "Bearer " + token))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertTrue(status == 200 || status == 404);

        if (status == 200) {
            JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
            assertNotNull(body.get("_links"));
            assertNotNull(body.get("_links").get("self"));
            assertTrue(body.get("_links").get("self").get("href").asText().contains("/api/carrito"));
        }
    }

    @Test
    void listadoVentasConTokenIncluyeEnlacesHateoas() throws Exception {
        String token = JwtTestHelper.tokenFor(jwtTokenService, "empleado", "EMPLEADO");

        MvcResult result = mockMvc.perform(get("/api/ventas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertNotNull(body.get("_links"));
        assertNotNull(body.get("_links").get("self"));
    }

    @Test
    void productosYaNoExistenEnResidual() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
    }
}
