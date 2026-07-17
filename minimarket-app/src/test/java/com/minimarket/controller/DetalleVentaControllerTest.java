package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.service.DetalleVentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DetalleVentaControllerTest {

    @Mock
    private DetalleVentaService detalleVentaService;

    @InjectMocks
    private DetalleVentaController detalleVentaController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private DetalleVenta detalleVenta;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(detalleVentaController).build();
        objectMapper = new ObjectMapper();

        Producto producto = new Producto();
        producto.setId(1L);

        detalleVenta = new DetalleVenta();
        detalleVenta.setId(1L);
        detalleVenta.setProducto(producto);
        detalleVenta.setCantidad(2);
        detalleVenta.setPrecio(1200.0);
    }

    @Test
    void listarDetalleVentas_retorna200ConLista() throws Exception {
        when(detalleVentaService.findAll()).thenReturn(List.of(detalleVenta));

        mockMvc.perform(get("/api/detalle-ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cantidad").value(2));
    }

    @Test
    void obtenerDetalleVentaPorId_encontrado_retorna200() throws Exception {
        when(detalleVentaService.findById(1L)).thenReturn(detalleVenta);

        mockMvc.perform(get("/api/detalle-ventas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.precio").value(1200.0));
    }

    @Test
    void obtenerDetalleVentaPorId_noEncontrado_retorna404() throws Exception {
        when(detalleVentaService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/detalle-ventas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void guardarDetalleVenta_retorna200() throws Exception {
        when(detalleVentaService.save(any(DetalleVenta.class))).thenReturn(detalleVenta);

        mockMvc.perform(post("/api/detalle-ventas")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(detalleVenta))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(2));
    }

    @Test
    void actualizarDetalleVenta_encontrado_retorna200() throws Exception {
        when(detalleVentaService.findById(1L)).thenReturn(detalleVenta);
        when(detalleVentaService.save(any(DetalleVenta.class))).thenReturn(detalleVenta);

        mockMvc.perform(put("/api/detalle-ventas/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(detalleVenta))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void actualizarDetalleVenta_noEncontrado_retorna404() throws Exception {
        when(detalleVentaService.findById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/detalle-ventas/99")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(detalleVenta))))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarDetalleVenta_encontrado_retorna204() throws Exception {
        when(detalleVentaService.findById(1L)).thenReturn(detalleVenta);

        mockMvc.perform(delete("/api/detalle-ventas/1"))
                .andExpect(status().isNoContent());

        verify(detalleVentaService).deleteById(1L);
    }

    @Test
    void eliminarDetalleVenta_noEncontrado_retorna404() throws Exception {
        when(detalleVentaService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/detalle-ventas/99"))
                .andExpect(status().isNotFound());
    }
}
