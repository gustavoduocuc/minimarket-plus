package com.minimarket.security;

import com.minimarket.security.service.JwtTokenService;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

public final class JwtTestHelper {

    private JwtTestHelper() {
    }

    public static String tokenFor(JwtTokenService jwtTokenService, String username, String roleName) {
        UserDetails userDetails = new User(
                username,
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_" + roleName)));
        return jwtTokenService.generateToken(userDetails);
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
