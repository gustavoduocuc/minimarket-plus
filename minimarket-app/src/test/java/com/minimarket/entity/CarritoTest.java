package com.minimarket.entity;

import com.minimarket.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarritoTest {

    private Usuario usuario;
    private Producto leche;
    private Producto pan;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(4L);
        usuario.setUsername("cliente");

        leche = new Producto();
        leche.setId(1L);
        leche.setNombre("Leche entera 1L");

        pan = new Producto();
        pan.setId(2L);
        pan.setNombre("Pan");

        carrito = new Carrito(usuario);
    }

    @Test
    void agregarProducto_creaItemNuevo() {
        carrito.agregarProducto(leche, 2, 10);

        assertEquals(1, carrito.getItems().size());
        assertEquals(2, carrito.getItems().get(0).getCantidad());
        assertEquals(leche, carrito.getItems().get(0).getProducto());
    }

    @Test
    void agregarProducto_mismoProducto_sumaCantidad() {
        carrito.agregarProducto(leche, 2, 10);
        carrito.agregarProducto(leche, 2, 10);

        assertEquals(1, carrito.getItems().size());
        assertEquals(4, carrito.getItems().get(0).getCantidad());
    }

    @Test
    void agregarProducto_otroProducto_agregaNuevoItem() {
        carrito.agregarProducto(leche, 2, 10);
        carrito.agregarProducto(pan, 1, 10);

        assertEquals(2, carrito.getItems().size());
        assertEquals(2, carrito.getItems().get(0).getCantidad());
        assertEquals(1, carrito.getItems().get(1).getCantidad());
    }

    @Test
    void agregarProducto_stockInsuficienteAcumulado_lanzaInsufficientStockException() {
        carrito.agregarProducto(leche, 2, 3);

        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () ->
                carrito.agregarProducto(leche, 2, 3));

        assertEquals("Stock insuficiente para 'Leche entera 1L'. Solo quedan 1 unidades.", exception.getMessage());
        assertEquals(1, exception.getDisponible());
    }

    @Test
    void quitarProducto_eliminaItemDelCarrito() {
        carrito.agregarProducto(leche, 2, 10);
        carrito.agregarProducto(pan, 1, 10);

        carrito.quitarProducto(leche.getId());

        assertEquals(1, carrito.getItems().size());
        assertEquals(pan, carrito.getItems().get(0).getProducto());
    }
}
