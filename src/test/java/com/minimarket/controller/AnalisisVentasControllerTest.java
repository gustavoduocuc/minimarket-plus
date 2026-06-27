package com.minimarket.controller;

import com.minimarket.dto.ProductoMasVendidoResponse;
import com.minimarket.service.AnalisisVentasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AnalisisVentasControllerTest {

    @Mock
    private AnalisisVentasService analisisVentasService;

    @InjectMocks
    private AnalisisVentasController analisisVentasController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(analisisVentasController).build();
    }

    @Test
    void productosMasVendidosReturns200ConRanking() throws Exception {
        ProductoMasVendidoResponse producto = new ProductoMasVendidoResponse(1L, "Arroz 1kg", 15);
        when(analisisVentasService.productosMasVendidos()).thenReturn(List.of(producto));

        mockMvc.perform(get("/api/ventas/analisis/productos-mas-vendidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productoId").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Arroz 1kg"))
                .andExpect(jsonPath("$[0].cantidadVendida").value(15));
    }
}
