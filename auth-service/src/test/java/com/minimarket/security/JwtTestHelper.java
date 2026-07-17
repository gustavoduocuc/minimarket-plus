package com.minimarket.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class JwtTestHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JwtTestHelper() {
    }

    public static String loginAndGetToken(MockMvc mockMvc, String username, String password) throws Exception {
        String responseBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = OBJECT_MAPPER.readTree(responseBody);
        return json.get("token").asText();
    }

    public static ResultActions performAuthenticated(
            MockMvc mockMvc,
            org.springframework.http.HttpMethod method,
            String uri,
            String token,
            String body) throws Exception {

        var requestBuilder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .request(method, uri)
                .contentType(MediaType.APPLICATION_JSON);

        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            requestBuilder.content(body);
        }

        return mockMvc.perform(requestBuilder);
    }
}
