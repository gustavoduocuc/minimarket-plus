package com.minimarket.service;

import com.minimarket.catalogo.Producto;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.ForbiddenOperationException;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.CarritoServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceImplTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoService productoService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private Usuario cliente;
    private Usuario empleado;
    private Usuario otroCliente;
    private Producto producto;

    @BeforeEach
    void setUp() {
        cliente = new Usuario();
        cliente.setId(4L);
        cliente.setUsername("cliente");

        empleado = new Usuario();
        empleado.setId(2L);
        empleado.setUsername("empleado");

        otroCliente = new Usuario();
        otroCliente.setId(5L);
        otroCliente.setUsername("otro");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche entera 1L");
        producto.setPrecio(1200.0);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void agregarProducto_clientePropioCarrito_agregaItem() {
        autenticarComo("cliente", "ROLE_CLIENTE");
        Carrito carritoVacio = new Carrito(cliente);
        when(usuarioService.ensure("cliente")).thenReturn(cliente);
        when(productoService.findById(1L)).thenReturn(producto);
        when(inventarioService.consultarStockDisponible(1L)).thenReturn(10);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carritoVacio));
        when(carritoRepository.save(carritoVacio)).thenReturn(carritoVacio);

        Carrito resultado = carritoService.agregarProducto("cliente", 4L, 1L, 2);

        assertEquals(1, resultado.getItems().size());
        assertEquals(2, resultado.getItems().get(0).getCantidad());
        assertEquals("Leche entera 1L", resultado.getItems().get(0).getNombreProducto());
        assertEquals(1200.0, resultado.getItems().get(0).getPrecio());
    }

    @Test
    void agregarProducto_clienteIntentaModificarCarritoAjeno_lanzaForbiddenOperationException() {
        autenticarComo("cliente", "ROLE_CLIENTE");
        when(usuarioService.ensure("cliente")).thenReturn(cliente);

        ForbiddenOperationException exception = assertThrows(ForbiddenOperationException.class, () ->
                carritoService.agregarProducto("cliente", 5L, 1L, 1));

        assertEquals("Un cliente solo puede modificar su propio carrito", exception.getClientMessage());
        verify(carritoRepository, never()).save(any());
    }

    @Test
    void agregarProducto_empleadoAgregaAlCarritoDeOtro_ok() {
        autenticarComo("empleado", "ROLE_EMPLEADO");
        Carrito carritoOtro = new Carrito(otroCliente);
        when(usuarioService.ensure("empleado")).thenReturn(empleado);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(otroCliente));
        when(productoService.findById(1L)).thenReturn(producto);
        when(inventarioService.consultarStockDisponible(1L)).thenReturn(10);
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(carritoOtro));
        when(carritoRepository.save(carritoOtro)).thenReturn(carritoOtro);

        Carrito resultado = carritoService.agregarProducto("empleado", 5L, 1L, 2);

        assertEquals(1, resultado.getItems().size());
        assertEquals(2, resultado.getItems().get(0).getCantidad());
    }

    @Test
    void agregarProducto_sinUsuarioObjetivo_usaCaller() {
        autenticarComo("cliente", "ROLE_CLIENTE");
        Carrito carritoVacio = new Carrito(cliente);
        when(usuarioService.ensure("cliente")).thenReturn(cliente);
        when(productoService.findById(1L)).thenReturn(producto);
        when(inventarioService.consultarStockDisponible(1L)).thenReturn(10);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carritoVacio));
        when(carritoRepository.save(carritoVacio)).thenReturn(carritoVacio);

        Carrito resultado = carritoService.agregarProducto("cliente", null, 1L, 2);

        assertEquals(1, resultado.getItems().size());
    }

    @Test
    void agregarProducto_usuarioSinProyeccionPrevia_usaEnsureLazy() {
        autenticarComo("nuevo", "ROLE_CLIENTE");
        Usuario nuevo = new Usuario();
        nuevo.setId(99L);
        nuevo.setUsername("nuevo");
        Carrito carritoVacio = new Carrito(nuevo);
        when(usuarioService.ensure("nuevo")).thenReturn(nuevo);
        when(productoService.findById(1L)).thenReturn(producto);
        when(inventarioService.consultarStockDisponible(1L)).thenReturn(10);
        when(carritoRepository.findByUsuarioId(99L)).thenReturn(Optional.of(carritoVacio));
        when(carritoRepository.save(carritoVacio)).thenReturn(carritoVacio);

        Carrito resultado = carritoService.agregarProducto("nuevo", null, 1L, 1);

        assertEquals(1, resultado.getItems().size());
        verify(usuarioService).ensure("nuevo");
    }

    @Test
    void agregarProducto_conCarritoExistente_sumaCantidad() {
        autenticarComo("cliente", "ROLE_CLIENTE");
        Carrito carritoExistente = new Carrito(cliente);
        carritoExistente.agregarProducto(1L, "Leche entera 1L", 1200.0, 2, 10);

        when(usuarioService.ensure("cliente")).thenReturn(cliente);
        when(productoService.findById(1L)).thenReturn(producto);
        when(inventarioService.consultarStockDisponible(1L)).thenReturn(10);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carritoExistente));
        when(carritoRepository.save(carritoExistente)).thenReturn(carritoExistente);

        Carrito resultado = carritoService.agregarProducto("cliente", 4L, 1L, 2);

        assertEquals(4, resultado.getItems().get(0).getCantidad());
    }

    @Test
    void agregarProducto_stockInsuficienteAcumulado_propagaInsufficientStockException() {
        autenticarComo("cliente", "ROLE_CLIENTE");
        Carrito carritoExistente = new Carrito(cliente);
        carritoExistente.agregarProducto(1L, "Leche entera 1L", 1200.0, 2, 3);

        when(usuarioService.ensure("cliente")).thenReturn(cliente);
        when(productoService.findById(1L)).thenReturn(producto);
        when(inventarioService.consultarStockDisponible(1L)).thenReturn(3);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carritoExistente));

        assertThrows(InsufficientStockException.class, () ->
                carritoService.agregarProducto("cliente", 4L, 1L, 2));

        verify(carritoRepository, never()).save(carritoExistente);
    }

    @Test
    void obtenerCarritoDe_devuelveCarritoDelUsuario() {
        Carrito carrito = new Carrito(cliente);
        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));

        Optional<Carrito> resultado = carritoService.obtenerCarritoDe("cliente");

        assertTrue(resultado.isPresent());
        assertEquals(cliente, resultado.get().getUsuario());
    }

    @Test
    void findAll_devuelveTodosLosCarritos() {
        Carrito carrito = new Carrito(cliente);
        when(carritoRepository.findAll()).thenReturn(List.of(carrito));

        assertEquals(1, carritoService.findAll().size());
    }

    @Test
    void findById_devuelveCarrito() {
        Carrito carrito = new Carrito(cliente);
        carrito.setId(1L);
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        assertTrue(carritoService.findById(1L).isPresent());
    }

    @Test
    void quitarProducto_eliminaItemDelCarrito() {
        Carrito carrito = new Carrito(cliente);
        carrito.agregarProducto(1L, "Leche entera 1L", 1200.0, 2, 10);

        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));

        carritoService.quitarProducto("cliente", 1L);

        assertTrue(carrito.getItems().isEmpty());
        verify(carritoRepository).save(carrito);
    }

    @Test
    void vaciarCarrito_eliminaTodosLosItems() {
        Carrito carrito = new Carrito(cliente);
        carrito.agregarProducto(1L, "Leche entera 1L", 1200.0, 2, 10);

        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(cliente));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));

        carritoService.vaciarCarrito("cliente");

        assertTrue(carrito.getItems().isEmpty());
        verify(carritoRepository).save(carrito);
    }

    private void autenticarComo(String username, String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities = java.util.Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities));
    }
}
