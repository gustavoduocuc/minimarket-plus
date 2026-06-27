package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.MetodoPago;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.service.CarritoCheckoutService;
import com.minimarket.service.CarritoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CarritoControllerTest {

    @Mock
    private CarritoService carritoService;

    @Mock
    private CarritoCheckoutService carritoCheckoutService;

    @InjectMocks
    private CarritoController carritoController;

    private MockMvc mockMvc;
    private Usuario usuario;
    private Producto producto;
    private Carrito carrito;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(carritoController).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("cliente", null));

        usuario = new Usuario();
        usuario.setId(4L);
        usuario.setUsername("cliente");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche entera 1L");
        producto.setStock(50);

        carrito = new Carrito(usuario);
        carrito.setId(1L);
        carrito.agregarProducto(producto, 2);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void obtenerCarritoPropioReturns200ConCarrito() throws Exception {
        when(carritoService.obtenerCarritoDe("cliente")).thenReturn(Optional.of(carrito));

        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.items[0].cantidad").value(2));
    }

    @Test
    void obtenerCarritoPropioReturns404SiNoExiste() throws Exception {
        when(carritoService.obtenerCarritoDe("cliente")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isNotFound());
    }

    @Test
    void agregarProductoAlCarritoReturns200ConCarritoActualizado() throws Exception {
        when(carritoService.agregarProducto("cliente", null, 1L, 2)).thenReturn(carrito);

        mockMvc.perform(post("/api/carrito")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"producto\":{\"id\":1},\"cantidad\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].cantidad").value(2));
    }

    @Test
    void agregarProductoAlCarritoConUsuarioObjetivo_delegaAlServicio() throws Exception {
        when(carritoService.agregarProducto("cliente", 4L, 1L, 2)).thenReturn(carrito);

        mockMvc.perform(post("/api/carrito")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"usuario\":{\"id\":4},\"producto\":{\"id\":1},\"cantidad\":2}"))
                .andExpect(status().isOk());

        verify(carritoService).agregarProducto("cliente", 4L, 1L, 2);
    }

    @Test
    void checkoutParaUsuarioReturns201() throws Exception {
        Venta venta = new Venta();
        venta.setId(42L);
        venta.setMetodoPago(MetodoPago.EFECTIVO);
        venta.setEstadoPago(EstadoPago.PENDIENTE_PAGO);

        when(carritoCheckoutService.checkoutParaUsuario(4L, MetodoPago.EFECTIVO)).thenReturn(venta);

        mockMvc.perform(post("/api/carrito/checkout/4")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"metodoPago\":\"EFECTIVO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ventaId").value(42));
    }

    @Test
    void listarTodosLosCarritosReturns200() throws Exception {
        when(carritoService.findAll()).thenReturn(List.of(carrito));

        mockMvc.perform(get("/api/carrito/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void obtenerCarritoPorIdReturns200() throws Exception {
        when(carritoService.findById(1L)).thenReturn(Optional.of(carrito));

        mockMvc.perform(get("/api/carrito/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void obtenerCarritoPorIdReturns404SiNoExiste() throws Exception {
        when(carritoService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/carrito/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void quitarProductoDelCarritoReturns204() throws Exception {
        mockMvc.perform(delete("/api/carrito/items/1"))
                .andExpect(status().isNoContent());

        verify(carritoService).quitarProducto("cliente", 1L);
    }

    @Test
    void vaciarCarritoReturns204() throws Exception {
        mockMvc.perform(delete("/api/carrito"))
                .andExpect(status().isNoContent());

        verify(carritoService).vaciarCarrito("cliente");
    }
}
