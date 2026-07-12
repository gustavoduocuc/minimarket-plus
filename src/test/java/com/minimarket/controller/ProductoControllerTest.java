package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.hateoas.HateoasTestSupport;
import com.minimarket.hateoas.ProductoModelAssembler;
import com.minimarket.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.stream.Stream;

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
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @Mock
    private ProductoModelAssembler productoModelAssembler;

    @InjectMocks
    private ProductoController productoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Producto producto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productoController).build();
        objectMapper = new ObjectMapper();

        Categoria categoria = new Categoria();
        categoria.setId(1L);

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz 1kg");
        producto.setPrecio(1500.0);
        producto.setCategoria(categoria);
        producto.setStockDisponible(50);

        HateoasTestSupport.stubProductoToModel(productoModelAssembler);
        when(productoModelAssembler.toCollectionModel(any()))
                .thenAnswer(invocation -> HateoasTestSupport.collectionModelOf(invocation.getArgument(0)));
    }

    @Test
    void listarProductos_retorna200ConLista() throws Exception {
        when(productoService.findAll()).thenReturn(List.of(producto));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerProductoPorId_encontrado_retorna200() throws Exception {
        when(productoService.findById(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Arroz 1kg"));
    }

    @Test
    void consultarStockDisponible_encontrado_retorna200() throws Exception {
        when(productoService.findById(1L)).thenReturn(producto);
        when(productoService.consultarStock(1L)).thenReturn(42);

        mockMvc.perform(get("/api/productos/1/stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productoId").value(1))
                .andExpect(jsonPath("$.stockDisponible").value(42));
    }

    @Test
    void consultarStockDisponible_noEncontrado_retorna404() throws Exception {
        when(productoService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/productos/99/stock"))
                .andExpect(status().isNotFound());
    }

    @Test
    void guardarProducto_retorna200() throws Exception {
        when(productoService.save(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(producto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Arroz 1kg"));
    }

    @Test
    void actualizarProducto_encontrado_retorna200() throws Exception {
        when(productoService.findById(1L)).thenReturn(producto);
        when(productoService.save(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(objectMapper.writeValueAsString(producto))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminarProducto_encontrado_retorna204() throws Exception {
        when(productoService.findById(1L)).thenReturn(producto);

        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());

        verify(productoService).deleteById(1L);
    }

    @ParameterizedTest(name = "{0} id inexistente retorna 404")
    @MethodSource("operacionesConIdInexistente")
    void operacionConIdInexistente_retorna404(String descripcion, OperacionHttp operacion) throws Exception {
        when(productoService.findById(99L)).thenReturn(null);

        operacion.ejecutar(mockMvc, objectMapper, producto);

        verify(productoService).findById(99L);
    }

    private static Stream<Object[]> operacionesConIdInexistente() {
        return Stream.of(
                new Object[]{"GET", (OperacionHttp) (mockMvc, objectMapper, producto) ->
                        mockMvc.perform(get("/api/productos/99")).andExpect(status().isNotFound())},
                new Object[]{"PUT", (OperacionHttp) (mockMvc, objectMapper, producto) ->
                        mockMvc.perform(put("/api/productos/99")
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .content(requireNonNull(objectMapper.writeValueAsString(producto))))
                                .andExpect(status().isNotFound())},
                new Object[]{"DELETE", (OperacionHttp) (mockMvc, objectMapper, producto) ->
                        mockMvc.perform(delete("/api/productos/99")).andExpect(status().isNotFound())}
        );
    }

    @FunctionalInterface
    private interface OperacionHttp {
        void ejecutar(MockMvc mockMvc, ObjectMapper objectMapper, Producto producto) throws Exception;
    }
}
