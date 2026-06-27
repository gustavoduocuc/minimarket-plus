package com.minimarket.service;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.impl.CarritoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceImplTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private Usuario usuario;
    private Producto producto;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(4L);
        usuario.setUsername("cliente");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche entera 1L");
        producto.setStock(10);
    }

    @Test
    void agregarProducto_carritoVacio_agregaPrimerItem() {
        Carrito carritoVacio = new Carrito(usuario);
        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carritoVacio));
        when(carritoRepository.save(carritoVacio)).thenReturn(carritoVacio);

        Carrito resultado = carritoService.agregarProducto("cliente", 1L, 2);

        assertEquals(1, resultado.getItems().size());
        assertEquals(2, resultado.getItems().get(0).getCantidad());
    }

    @Test
    void agregarProducto_conCarritoExistente_sumaCantidad() {
        Carrito carritoExistente = new Carrito(usuario);
        carritoExistente.agregarProducto(producto, 2);

        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carritoExistente));
        when(carritoRepository.save(carritoExistente)).thenReturn(carritoExistente);

        Carrito resultado = carritoService.agregarProducto("cliente", 1L, 2);

        assertEquals(1, resultado.getItems().size());
        assertEquals(4, resultado.getItems().get(0).getCantidad());
    }

    @Test
    void agregarProducto_stockInsuficienteAcumulado_propagaInsufficientStockException() {
        producto.setStock(3);
        Carrito carritoExistente = new Carrito(usuario);
        carritoExistente.agregarProducto(producto, 2);

        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carritoExistente));

        assertThrows(InsufficientStockException.class, () ->
                carritoService.agregarProducto("cliente", 1L, 2));

        verify(carritoRepository, never()).save(carritoExistente);
    }

    @Test
    void obtenerCarritoDe_devuelveCarritoDelUsuario() {
        Carrito carrito = new Carrito(usuario);
        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));

        Optional<Carrito> resultado = carritoService.obtenerCarritoDe("cliente");

        assertTrue(resultado.isPresent());
        assertEquals(usuario, resultado.get().getUsuario());
    }

    @Test
    void findAll_devuelveTodosLosCarritos() {
        Carrito carrito = new Carrito(usuario);
        when(carritoRepository.findAll()).thenReturn(List.of(carrito));

        List<Carrito> resultados = carritoService.findAll();

        assertEquals(1, resultados.size());
    }

    @Test
    void findById_devuelveCarrito() {
        Carrito carrito = new Carrito(usuario);
        carrito.setId(1L);
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        Optional<Carrito> resultado = carritoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
    }

    @Test
    void quitarProducto_eliminaItemDelCarrito() {
        Carrito carrito = new Carrito(usuario);
        carrito.agregarProducto(producto, 2);

        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));

        carritoService.quitarProducto("cliente", 1L);

        assertTrue(carrito.getItems().isEmpty());
        verify(carritoRepository).save(carrito);
    }

    @Test
    void vaciarCarrito_eliminaTodosLosItems() {
        Carrito carrito = new Carrito(usuario);
        carrito.agregarProducto(producto, 2);

        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));

        carritoService.vaciarCarrito("cliente");

        assertTrue(carrito.getItems().isEmpty());
        verify(carritoRepository).save(carrito);
    }
}
