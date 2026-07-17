package com.minimarket.entity;

import com.minimarket.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarritoTest {

    private Usuario usuario;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(4L);
        usuario.setUsername("cliente");
        carrito = new Carrito(usuario);
    }

    @Test
    void agregarProducto_creaItemNuevoConSnapshot() {
        carrito.agregarProducto(1L, "Leche entera 1L", 1200.0, 2, 10);

        assertEquals(1, carrito.getItems().size());
        assertEquals(2, carrito.getItems().get(0).getCantidad());
        assertEquals(1L, carrito.getItems().get(0).getProductoId());
        assertEquals("Leche entera 1L", carrito.getItems().get(0).getNombreProducto());
        assertEquals(1200.0, carrito.getItems().get(0).getPrecio());
    }

    @Test
    void agregarProducto_mismoProducto_sumaCantidad() {
        carrito.agregarProducto(1L, "Leche entera 1L", 1200.0, 2, 10);
        carrito.agregarProducto(1L, "Leche entera 1L", 1200.0, 2, 10);

        assertEquals(1, carrito.getItems().size());
        assertEquals(4, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void agregarProducto_otroProducto_agregaNuevoItem() {
        carrito.agregarProducto(1L, "Leche entera 1L", 1200.0, 2, 10);
        carrito.agregarProducto(2L, "Pan", 500.0, 1, 10);

        assertEquals(2, carrito.getItems().size());
        assertEquals(2, carrito.getItems().get(0).getCantidad());
        assertEquals(1, carrito.getItems().get(1).getCantidad());
    }

    @Test
    void agregarProducto_stockInsuficienteAcumulado_lanzaInsufficientStockException() {
        carrito.agregarProducto(1L, "Leche entera 1L", 1200.0, 2, 3);

        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () ->
                carrito.agregarProducto(1L, "Leche entera 1L", 1200.0, 2, 3));

        assertEquals("Stock insuficiente para 'Leche entera 1L'. Solo quedan 1 unidades.", exception.getMessage());
        assertEquals(1, exception.getDisponible());
    }

    @Test
    void quitarProducto_eliminaItemDelCarrito() {
        carrito.agregarProducto(1L, "Leche entera 1L", 1200.0, 2, 10);
        carrito.agregarProducto(2L, "Pan", 500.0, 1, 10);

        carrito.quitarProducto(1L);

        assertEquals(1, carrito.getItems().size());
        assertEquals(2L, carrito.getItems().get(0).getProductoId());
    }
}
