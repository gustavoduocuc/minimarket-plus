package com.minimarket.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class VentasUsuarioClient {

    private static final Logger log = LoggerFactory.getLogger(VentasUsuarioClient.class);

    private final RestClient ventasRestClient;
    private final String internalToken;

    public VentasUsuarioClient(
            RestClient ventasRestClient,
            @Value("${ventas.internal-token:MiniMarketInternalTokenLocal}") String internalToken) {
        this.ventasRestClient = ventasRestClient;
        this.internalToken = internalToken;
    }

    public void ensureUsuario(String username) {
        try {
            ventasRestClient.post()
                    .uri("/internal/usuarios")
                    .header("X-Internal-Token", internalToken)
                    .body(Map.of("username", username))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.warn("No se pudo sincronizar usuario '{}' con ventas-service: {}", username, ex.getMessage());
        }
    }
}
