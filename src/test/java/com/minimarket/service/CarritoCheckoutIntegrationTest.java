package com.minimarket.service;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.MetodoPago;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class CarritoCheckoutIntegrationTest {

    @Autowired
    private CarritoCheckoutService carritoCheckoutService;

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Test
    void checkoutWithRealDatabaseCompletesWithoutError() {
        Usuario cliente = usuarioRepository.findByUsername("cliente").orElseThrow();
        Producto producto = productoRepository.findAll().get(0);

        carritoRepository.findByUsuarioId(cliente.getId()).ifPresent(carritoRepository::delete);

        carritoService.agregarProducto("cliente", null, producto.getId(), 1);

        assertDoesNotThrow(() -> carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO));
    }
}
