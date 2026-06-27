package com.minimarket.controller;

import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VentaControllerConfirmarPagoTest {

    @Mock
    private VentaService ventaService;

    @InjectMocks
    private VentaController ventaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ventaController).build();
    }

    @Test
    void confirmarPagoReturns200WithEstadoPagado() throws Exception {
        Venta venta = new Venta();
        venta.setId(42L);
        venta.setEstadoPago(EstadoPago.PAGADO);

        when(ventaService.confirmarPago(42L)).thenReturn(venta);

        mockMvc.perform(post("/api/ventas/42/confirmar-pago"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ventaId").value(42))
                .andExpect(jsonPath("$.estadoPago").value("PAGADO"));
    }
}
