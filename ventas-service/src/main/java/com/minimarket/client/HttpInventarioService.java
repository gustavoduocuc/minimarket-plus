package com.minimarket.client;

import com.minimarket.exception.CatalogoServiceUnavailableException;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.service.InventarioService;
import com.minimarket.service.ProductoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
public class HttpInventarioService implements InventarioService {

    private final RestClient catalogoRestClient;
    private final BearerTokenPropagator bearerTokenPropagator;
    private final ProductoService productoService;
    private final ObjectMapper objectMapper;

    public HttpInventarioService(
            RestClient catalogoRestClient,
            BearerTokenPropagator bearerTokenPropagator,
            ProductoService productoService,
            ObjectMapper objectMapper) {
        this.catalogoRestClient = catalogoRestClient;
        this.bearerTokenPropagator = bearerTokenPropagator;
        this.productoService = productoService;
        this.objectMapper = objectMapper;
    }

    @Override
    public int consultarStockDisponible(Long productoId) {
        return productoService.consultarStock(productoId);
    }

    @Override
    public void registrarSalida(Long productoId, int cantidad) {
        try {
            catalogoRestClient.post()
                    .uri("/api/inventario/salidas")
                    .headers(bearerTokenPropagator.fromCurrentRequest())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("productoId", productoId, "cantidad", cantidad))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 422) {
                throw mapInsufficientStock(ex, productoId, cantidad);
            }
            if (ex.getStatusCode().is4xxClientError()) {
                throw new IllegalArgumentException(
                        "No se pudo registrar salida de stock para producto " + productoId);
            }
            throw new CatalogoServiceUnavailableException(
                    "Catálogo-inventario no disponible al descontar stock", ex);
        } catch (RestClientException ex) {
            throw new CatalogoServiceUnavailableException(
                    "Catálogo-inventario no disponible al descontar stock", ex);
        }
    }

    private InsufficientStockException mapInsufficientStock(
            RestClientResponseException ex, Long productoId, int cantidad) {
        try {
            JsonNode body = objectMapper.readTree(ex.getResponseBodyAsString());
            String producto = body.path("producto").asText("producto-" + productoId);
            int disponible = body.path("disponible").asInt(0);
            int solicitado = body.path("solicitado").asInt(cantidad);
            return new InsufficientStockException(producto, disponible, solicitado);
        } catch (Exception parseException) {
            return new InsufficientStockException("producto-" + productoId, 0, cantidad);
        }
    }
}
