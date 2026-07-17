package com.minimarket.client;

import com.minimarket.catalogo.Producto;
import com.minimarket.catalogo.StockDisponibleResponse;
import com.minimarket.exception.CatalogoServiceUnavailableException;
import com.minimarket.service.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class HttpProductoService implements ProductoService {

    private final RestClient catalogoRestClient;

    public HttpProductoService(RestClient catalogoRestClient) {
        this.catalogoRestClient = catalogoRestClient;
    }

    @Override
    public Producto findById(Long id) {
        if (id == null) {
            return null;
        }
        try {
            return catalogoRestClient.get()
                    .uri("/api/productos/{id}", id)
                    .retrieve()
                    .body(Producto.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return null;
            }
            throw new CatalogoServiceUnavailableException(
                    "No se pudo consultar el producto en catálogo-inventario", ex);
        } catch (RestClientException ex) {
            throw new CatalogoServiceUnavailableException(
                    "No se pudo consultar el producto en catálogo-inventario", ex);
        }
    }

    @Override
    public int consultarStock(Long productoId) {
        if (productoId == null) {
            throw new IllegalArgumentException("Producto inválido");
        }
        try {
            StockDisponibleResponse response = catalogoRestClient.get()
                    .uri("/api/productos/{id}/stock", productoId)
                    .retrieve()
                    .body(StockDisponibleResponse.class);
            if (response == null || response.getStockDisponible() == null) {
                throw new CatalogoServiceUnavailableException(
                        "Respuesta de stock inválida desde catálogo-inventario");
            }
            return response.getStockDisponible();
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new IllegalArgumentException("Producto con id " + productoId + " no encontrado");
            }
            throw new CatalogoServiceUnavailableException(
                    "No se pudo consultar el stock en catálogo-inventario", ex);
        } catch (RestClientException ex) {
            throw new CatalogoServiceUnavailableException(
                    "No se pudo consultar el stock en catálogo-inventario", ex);
        }
    }
}
