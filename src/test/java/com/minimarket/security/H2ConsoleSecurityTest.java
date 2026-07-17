package com.minimarket.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class H2ConsoleSecurityTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void h2ConsoleIsAccessibleWithoutAuthenticationInDevProfile() {
        ResponseEntity<String> response = restTemplate.getForEntity("/h2-console/", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getHeaders().getOrEmpty("X-Frame-Options").contains("SAMEORIGIN"));
    }
}
