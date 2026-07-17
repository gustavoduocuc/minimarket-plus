package com.minimarket.service;

import com.minimarket.catalogo.Producto;
import com.minimarket.entity.*;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.repository.CarritoRepository;
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
    private ProductoService productoService;

    @Mock
    private VentaService ventaService;

    @Mock
    private PaymentProcessor paymentProcessor;

    @Mock
    private NotificacionService notificacionService;

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

        carrito = new Carrito(usuario);
        carrito.setId(1L);
        carrito.agregarProducto(1L, "Cafe", 1000.0, 2, 10);
    }

    @Test
    void checkout_conCarritoVacio_lanzaIllegalStateException() {
        when(usuarioService.ensure("cliente")).thenReturn(usuario);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO, null));

        assertEquals("No hay productos en el carrito", exception.getMessage());
        verify(ventaService, never()).save(any());
    }

    @Test
    void checkout_conUnProducto_creaVentaConSnapshotsYEstadoPendiente() {
        when(usuarioService.ensure("cliente")).thenReturn(usuario);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoService.findById(1L)).thenReturn(producto);

        Venta ventaGuardada = new Venta();
        ventaGuardada.setId(42L);
        ventaGuardada.setMetodoPago(MetodoPago.DEBITO);
        ventaGuardada.setEstadoPago(EstadoPago.PENDIENTE_PAGO);
        when(ventaService.save(any(Venta.class))).thenReturn(ventaGuardada);

        Venta resultado = carritoCheckoutService.checkout("cliente", MetodoPago.DEBITO, null);

        ArgumentCaptor<Venta> ventaCaptor = ArgumentCaptor.forClass(Venta.class);
        verify(ventaService).save(ventaCaptor.capture());
        Venta ventaEnviada = ventaCaptor.getValue();

        assertEquals(MetodoPago.DEBITO, ventaEnviada.getMetodoPago());
        assertEquals(EstadoPago.PENDIENTE_PAGO, ventaEnviada.getEstadoPago());
        assertEquals(TipoEntrega.RETIRO_EN_TIENDA, ventaEnviada.getTipoEntrega());
        assertEquals(1, ventaEnviada.getDetalles().size());
        assertEquals(1L, ventaEnviada.getDetalles().get(0).getProductoId());
        assertEquals("Cafe", ventaEnviada.getDetalles().get(0).getNombreProducto());
        assertEquals(1000.0, ventaEnviada.getDetalles().get(0).getPrecio());
        assertEquals(2, ventaEnviada.getDetalles().get(0).getCantidad());
        assertEquals(42L, resultado.getId());
    }

    @Test
    void checkout_conMultiplesProductos_creaDetallesPorProducto() {
        Producto pan = new Producto();
        pan.setId(2L);
        pan.setNombre("Pan");
        pan.setPrecio(500.0);
        carrito.agregarProducto(2L, "Pan", 500.0, 3, 10);

        when(usuarioService.ensure("cliente")).thenReturn(usuario);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoService.findById(1L)).thenReturn(producto);
        when(productoService.findById(2L)).thenReturn(pan);
        when(ventaService.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        carritoCheckoutService.checkout("cliente", MetodoPago.CREDITO, null);

        ArgumentCaptor<Venta> ventaCaptor = ArgumentCaptor.forClass(Venta.class);
        verify(ventaService).save(ventaCaptor.capture());
        assertEquals(2, ventaCaptor.getValue().getDetalles().size());
    }

    @Test
    void checkout_sinStock_propagaInsufficientStockException() {
        when(usuarioService.ensure("cliente")).thenReturn(usuario);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoService.findById(1L)).thenReturn(producto);
        when(ventaService.save(any(Venta.class)))
                .thenThrow(new InsufficientStockException("Cafe", 1, 2));

        assertThrows(InsufficientStockException.class, () ->
                carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO, null));

        verify(carritoRepository, never()).delete(requireNonNull(carrito));
    }

    @Test
    void checkout_exitoso_borraCarritoDelUsuario() {
        when(usuarioService.ensure("cliente")).thenReturn(usuario);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoService.findById(1L)).thenReturn(producto);
        when(ventaService.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO, null);

        verify(carritoRepository).delete(requireNonNull(carrito));
    }

    @Test
    void checkoutParaUsuario_concretaVentaDelTercero() {
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoService.findById(1L)).thenReturn(producto);

        Venta ventaGuardada = new Venta();
        ventaGuardada.setId(99L);
        when(ventaService.save(any(Venta.class))).thenReturn(ventaGuardada);

        Venta resultado = carritoCheckoutService.checkoutParaUsuario(4L, MetodoPago.EFECTIVO, null);

        assertEquals(99L, resultado.getId());
        verify(carritoRepository).delete(requireNonNull(carrito));
    }

    @Test
    void checkout_exitoso_invocaPaymentProcessor() {
        Venta ventaGuardada = new Venta();
        ventaGuardada.setId(10L);
        ventaGuardada.setEstadoPago(EstadoPago.PENDIENTE_PAGO);

        when(usuarioService.ensure("cliente")).thenReturn(usuario);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoService.findById(1L)).thenReturn(producto);
        when(ventaService.save(any(Venta.class))).thenReturn(ventaGuardada);

        carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO, null);

        verify(paymentProcessor).initiatePayment(ventaGuardada);
    }

    @Test
    void checkout_exitoso_notificaCambioDePedidoAlCliente() {
        Venta ventaGuardada = new Venta();
        ventaGuardada.setId(42L);
        ventaGuardada.setUsuario(usuario);
        ventaGuardada.setEstadoPago(EstadoPago.PENDIENTE_PAGO);

        when(usuarioService.ensure("cliente")).thenReturn(usuario);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoService.findById(1L)).thenReturn(producto);
        when(ventaService.save(any(Venta.class))).thenReturn(ventaGuardada);

        carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO, null);

        verify(notificacionService).notificarCambioPedido(
                ventaGuardada,
                "Pedido #42 creado, pendiente de pago");
    }

    @Test
    void checkout_conDespachoDomicilio_persisteTipoEntrega() {
        when(usuarioService.ensure("cliente")).thenReturn(usuario);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoService.findById(1L)).thenReturn(producto);
        when(ventaService.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO, TipoEntrega.DESPACHO_DOMICILIO);

        ArgumentCaptor<Venta> ventaCaptor = ArgumentCaptor.forClass(Venta.class);
        verify(ventaService).save(ventaCaptor.capture());
        assertEquals(TipoEntrega.DESPACHO_DOMICILIO, ventaCaptor.getValue().getTipoEntrega());
    }

    @Test
    void checkout_sinTipoEntrega_usaRetiroEnTiendaPorDefecto() {
        when(usuarioService.ensure("cliente")).thenReturn(usuario);
        when(carritoRepository.findByUsuarioId(4L)).thenReturn(Optional.of(carrito));
        when(productoService.findById(1L)).thenReturn(producto);
        when(ventaService.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        carritoCheckoutService.checkout("cliente", MetodoPago.EFECTIVO, null);

        ArgumentCaptor<Venta> ventaCaptor = ArgumentCaptor.forClass(Venta.class);
        verify(ventaService).save(ventaCaptor.capture());
        assertEquals(TipoEntrega.RETIRO_EN_TIENDA, ventaCaptor.getValue().getTipoEntrega());
    }
}
