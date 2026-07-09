package com.minimarket.controller;

import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.Venta;
import com.minimarket.hateoas.HateoasTestSupport;
import com.minimarket.hateoas.VentaModelAssembler;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VentaControllerPendientesTest {

    @Mock
    private VentaService ventaService;

    @Mock
    private VentaModelAssembler ventaModelAssembler;

    @InjectMocks
    private VentaController ventaController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ventaController).build();

        when(ventaModelAssembler.toPendientesCollectionModel(any()))
                .thenAnswer(invocation -> HateoasTestSupport.collectionModelOf(invocation.getArgument(0)));
    }

    @Test
    void listarPendientesReturns200ConVentasPendientes() throws Exception {
        Venta venta = new Venta();
        venta.setId(7L);
        venta.setEstadoPago(EstadoPago.PENDIENTE_PAGO);
        when(ventaService.findPendientesDePago()).thenReturn(List.of(venta));

        mockMvc.perform(get("/api/ventas/pendientes"))
                .andExpect(status().isOk());
    }
}
