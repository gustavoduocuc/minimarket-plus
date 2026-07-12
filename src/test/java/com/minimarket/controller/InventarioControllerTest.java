package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.hateoas.HateoasTestSupport;
import com.minimarket.hateoas.InventarioModelAssembler;
import com.minimarket.service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class InventarioControllerTest {

    @Mock
    private InventarioService inventarioService;

    @Mock
    private InventarioModelAssembler inventarioModelAssembler;

    @InjectMocks
    private InventarioController inventarioController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Inventario inventario;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventarioController).build();
        objectMapper = new ObjectMapper();

        Producto producto = new Producto();
        producto.setId(1L);

        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(10);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());

        HateoasTestSupport.stubInventarioModelAssembler(inventarioModelAssembler);
    }

    @Test
    void listarMovimientos_retorna200ConLista() throws Exception {
        when(inventarioService.findAll()).thenReturn(List.of(inventario));

        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerMovimientoPorId_encontrado_retorna200() throws Exception {
        when(inventarioService.findById(1L)).thenReturn(inventario);

        mockMvc.perform(get("/api/inventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipoMovimiento").value("Entrada"));
    }

    @Test
    void obtenerMovimientoPorId_noEncontrado_retorna404() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/inventario/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrarMovimiento_retorna200() throws Exception {
        when(inventarioService.save(any(Inventario.class))).thenReturn(inventario);

        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(inventario))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(10));
    }

    @Test
    void actualizarMovimiento_encontrado_retorna200() throws Exception {
        when(inventarioService.findById(1L)).thenReturn(inventario);
        when(inventarioService.save(any(Inventario.class))).thenReturn(inventario);

        mockMvc.perform(put("/api/inventario/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(inventario))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void actualizarMovimiento_noEncontrado_retorna404() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/inventario/99")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(inventario))))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarMovimiento_encontrado_retorna204() throws Exception {
        when(inventarioService.findById(1L)).thenReturn(inventario);

        mockMvc.perform(delete("/api/inventario/1"))
                .andExpect(status().isNoContent());

        verify(inventarioService).deleteById(1L);
    }

    @Test
    void eliminarMovimiento_noEncontrado_retorna404() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/inventario/99"))
                .andExpect(status().isNotFound());
    }
}
