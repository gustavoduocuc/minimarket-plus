package com.minimarket.service;

import com.minimarket.entity.*;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.CarritoCheckoutServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoCheckoutServiceImplTest {

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private VentaService ventaService;

    @Mock
    private PaymentProcessor paymentProcessor;

    @InjectMocks
    private CarritoCheckoutServiceImpl carritoCheckoutService;

    private Usuario usuario;
    private Producto producto;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(4L);
        usuario.setUsername("cliente");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Cafe");
        producto.setPrecio(1000.0);
        producto.setStock(10);

        carrito = new Carrito(usuario);
        carrito.setId(1L);
        carrito.agregarProducto(producto, 2);
    }

    @Test
    void checkout_conCarritoVacio_lanzaIllegalStateException() {
        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO));

        assertEquals("No hay productos en el carrito", exception.getMessage());
        verify(ventaService, never()).save(any());
    }

    @Test
    void checkout_conUnProducto_creaVentaConMetodoPagoYEstadoPendiente() {
        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Venta ventaGuardada = new Venta();
        ventaGuardada.setId(42L);
        ventaGuardada.setMetodoPago(MetodoPago.DEBITO);
        ventaGuardada.setEstadoPago(EstadoPago.PENDIENTE_PAGO);
        when(ventaService.save(any(Venta.class))).thenReturn(ventaGuardada);

        Venta resultado = carritoCheckoutService.checkout("cliente", MetodoPago.DEBITO);

        ArgumentCaptor<Venta> ventaCaptor = ArgumentCaptor.forClass(Venta.class);
        verify(ventaService).save(ventaCaptor.capture());
        Venta ventaEnviada = ventaCaptor.getValue();

        assertEquals(MetodoPago.DEBITO, ventaEnviada.getMetodoPago());
        assertEquals(EstadoPago.PENDIENTE_PAGO, ventaEnviada.getEstadoPago());
        assertEquals(1, ventaEnviada.getDetalles().size());
        assertEquals(2, ventaEnviada.getDetalles().get(0).getCantidad());
        assertEquals(42L, resultado.getId());
    }

    @Test
    void checkout_conMultiplesProductos_creaDetallesPorProducto() {
        Producto pan = new Producto();
        pan.setId(2L);
        pan.setNombre("Pan");
        pan.setPrecio(500.0);
        pan.setStock(10);
        carrito.agregarProducto(pan, 3);

        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(pan));
        when(ventaService.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        carritoCheckoutService.checkout("cliente", MetodoPago.CREDITO);

        ArgumentCaptor<Venta> ventaCaptor = ArgumentCaptor.forClass(Venta.class);
        verify(ventaService).save(ventaCaptor.capture());
        assertEquals(2, ventaCaptor.getValue().getDetalles().size());
    }

    @Test
    void checkout_sinStock_propagaInsufficientStockException() {
        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(ventaService.save(any(Venta.class)))
                .thenThrow(new InsufficientStockException("Cafe", 1, 2));

        assertThrows(InsufficientStockException.class, () ->
                carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO));

        verify(carritoRepository, never()).delete(requireNonNull(carrito));
    }

    @Test
    void checkout_exitoso_borraCarritoDelUsuario() {
        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(ventaService.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO);

        verify(carritoRepository).delete(requireNonNull(carrito));
    }

    @Test
    void checkoutParaUsuario_concretaVentaDelTercero() {
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Venta ventaGuardada = new Venta();
        ventaGuardada.setId(99L);
        when(ventaService.save(any(Venta.class))).thenReturn(ventaGuardada);

        Venta resultado = carritoCheckoutService.checkoutParaUsuario(4L, MetodoPago.EFECTIVO);

        assertEquals(99L, resultado.getId());
        verify(carritoRepository).delete(requireNonNull(carrito));
    }

    @Test
    void checkout_exitoso_invocaPaymentProcessor() {
        Venta ventaGuardada = new Venta();
        ventaGuardada.setId(10L);
        ventaGuardada.setEstadoPago(EstadoPago.PENDIENTE_PAGO);

        when(usuarioService.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(ventaService.save(any(Venta.class))).thenReturn(ventaGuardada);

        carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO);

        verify(paymentProcessor).initiatePayment(ventaGuardada);
    }
}
