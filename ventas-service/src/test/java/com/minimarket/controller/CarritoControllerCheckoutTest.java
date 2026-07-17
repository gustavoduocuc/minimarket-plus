package com.minimarket.controller;

import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.MetodoPago;
import com.minimarket.entity.TipoEntrega;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CarritoControllerCheckoutTest {

    @Mock
    private CarritoService carritoService;

    @Mock
    private CarritoCheckoutService carritoCheckoutService;

    @InjectMocks
    private CarritoController carritoController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(carritoController).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("cliente", null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void checkoutReturns201WithCheckoutResponse() throws Exception {
        Venta venta = new Venta();
        venta.setId(42L);
        venta.setMetodoPago(MetodoPago.DEBITO);
        venta.setEstadoPago(EstadoPago.PENDIENTE_PAGO);
        venta.setTipoEntrega(TipoEntrega.RETIRO_EN_TIENDA);

        when(carritoCheckoutService.checkout(eq("cliente"), eq(MetodoPago.DEBITO), isNull())).thenReturn(venta);

        mockMvc.perform(post("/api/carrito/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metodoPago\":\"DEBITO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ventaId").value(42))
                .andExpect(jsonPath("$.metodoPago").value("DEBITO"))
                .andExpect(jsonPath("$.estadoPago").value("PENDIENTE_PAGO"))
                .andExpect(jsonPath("$.tipoEntrega").value("RETIRO_EN_TIENDA"));
    }

    @Test
    void checkoutWithTipoEntregaDespachoReturns201() throws Exception {
        Venta venta = new Venta();
        venta.setId(43L);
        venta.setMetodoPago(MetodoPago.EFECTIVO);
        venta.setEstadoPago(EstadoPago.PENDIENTE_PAGO);
        venta.setTipoEntrega(TipoEntrega.DESPACHO_DOMICILIO);

        when(carritoCheckoutService.checkout(
                eq("cliente"), eq(MetodoPago.EFECTIVO), eq(TipoEntrega.DESPACHO_DOMICILIO)))
                .thenReturn(venta);

        mockMvc.perform(post("/api/carrito/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metodoPago\":\"EFECTIVO\",\"tipoEntrega\":\"DESPACHO_DOMICILIO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoEntrega").value("DESPACHO_DOMICILIO"));
    }
}
