package com.minimarket.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityHeadersAndXssTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void xFrameOptionsHeaderIsDeny() throws Exception {
        mockMvc.perform(get("/public/hola"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void xContentTypeOptionsHeaderIsNosniff() throws Exception {
        mockMvc.perform(get("/public/hola"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void contentSecurityPolicyHeaderIsPresent() throws Exception {
        mockMvc.perform(get("/public/hola"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", "default-src 'self'"));
    }

    @Test
    void xssPayloadInLoginUsernameIsHandledSafely() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"username\":\"<script>alert(1)</script>\",\"password\":\"Admin123!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void responseBodyDoesNotReflectRawHtml() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"username\":\"<script>alert(1)</script>\",\"password\":\"Admin123!\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    if (body.contains("<script>")) {
                        throw new AssertionError("Response must not reflect raw script tags");
                    }
                });
    }
}
