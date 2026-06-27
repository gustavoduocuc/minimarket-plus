package com.minimarket.security;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CarritoOwnershipIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private String clienteToken;

    @BeforeAll
    void setUp() throws Exception {
        clienteToken = JwtTestHelper.loginAndGetToken(mockMvc, "cliente", "Cliente123!");
    }

    @Test
    void clienteNoPuedeAgregarProductosAlCarritoDeOtroUsuario() throws Exception {
        Usuario admin = usuarioRepository.findByUsername("admin").orElseThrow();
        String body = """
                {
                  "usuario": { "id": %d },
                  "producto": { "id": 1 },
                  "cantidad": 1
                }
                """.formatted(admin.getId());

        JwtTestHelper.performAuthenticated(
                        mockMvc,
                        HttpMethod.POST,
                        "/api/carrito",
                        clienteToken,
                        body)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error")
                        .value("Un cliente solo puede modificar su propio carrito"));
    }
}
