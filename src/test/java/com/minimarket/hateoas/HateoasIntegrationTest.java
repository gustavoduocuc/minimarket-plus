package com.minimarket.hateoas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HateoasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void productoPorIdIncluyeEnlacesHateoas() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertNotNull(body.get("_links"));
        assertNotNull(body.get("_links").get("self"));
        assertTrue(body.get("_links").get("self").get("href").asText().contains("/api/productos/1"));
        assertNotNull(body.get("_links").get("productos"));
        assertNotNull(body.get("_links").get("stock"));
    }

    @Test
    void listadoProductosIncluyeEnlacesHateoas() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertNotNull(body.get("_links"));
        assertNotNull(body.get("_links").get("self"));
        assertNotNull(body.get("_embedded"));
    }

    @Test
    void inventarioConTokenIncluyeEnlacesHateoas() throws Exception {
        String token = obtenerToken("gerente", "Gerente123!");

        MvcResult result = mockMvc.perform(get("/api/inventario")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertNotNull(body.get("_links"));
        assertNotNull(body.get("_links").get("inventario"));
        assertNotNull(body.get("_links").get("registrarMovimiento"));
    }

    @Test
    void usuariosSinTokenRetorna401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    private String obtenerToken(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
    }
}
