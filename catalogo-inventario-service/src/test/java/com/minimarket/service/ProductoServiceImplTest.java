package com.minimarket.service;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.exception.InvalidRequestException;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto producto;
    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria();
        categoria.setId(1L);

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setCategoria(categoria);
    }

    @Test
    void testFindAll() {
        when(productoRepository.findAll()).thenReturn(Arrays.asList(producto));
        when(inventarioService.consultarStockDisponible(1L)).thenReturn(100);

        List<Producto> productos = productoService.findAll();

        assertFalse(productos.isEmpty());
        assertEquals(1, productos.size());
        assertEquals(100, productos.get(0).getStockDisponible());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Encontrado() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(inventarioService.consultarStockDisponible(1L)).thenReturn(100);

        Producto resultado = productoService.findById(1L);

        assertNotNull(resultado);
        assertEquals("Arroz", resultado.getNombre());
        assertEquals(100, resultado.getStockDisponible());
    }

    @Test
    void testFindById_NoEncontrado() {
        when(productoRepository.findById(2L)).thenReturn(Optional.empty());
        Producto resultado = productoService.findById(2L);
        assertNull(resultado);
    }

    @Test
    void consultarStock_productoExistente_retornaStockDesdeInventario() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(inventarioService.consultarStockDisponible(1L)).thenReturn(42);

        int stock = productoService.consultarStock(1L);

        assertEquals(42, stock);
    }

    @Test
    void consultarStock_productoInexistente_lanzaInvalidRequestException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class, () -> productoService.consultarStock(99L));
    }

    @Test
    void testSave_Exito() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(requireNonNull(producto))).thenReturn(requireNonNull(producto));
        when(inventarioService.consultarStockDisponible(1L)).thenReturn(0);

        Producto resultado = productoService.save(producto);

        assertNotNull(resultado);
        verify(categoriaRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(requireNonNull(producto));
    }

    @Test
    void testSave_SinCategoria_LanzaExcepcion() {
        producto.setCategoria(null);

        assertThrows(InvalidRequestException.class, () -> {
            productoService.save(producto);
        });

        verifyNoInteractions(productoRepository);
    }

    @Test
    void testSave_CategoriaSinId_LanzaExcepcion() {
        categoria.setId(null);
        producto.setCategoria(categoria);

        assertThrows(InvalidRequestException.class, () -> {
            productoService.save(producto);
        });

        verifyNoInteractions(productoRepository);
    }

    @Test
    void testSave_CategoriaNoExisteEnBD_LanzaExcepcion() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InvalidRequestException.class, () -> {
            productoService.save(producto);
        });

        verifyNoInteractions(productoRepository);
    }

    @Test
    void testDeleteById() {
        doNothing().when(productoRepository).deleteById(1L);
        productoService.deleteById(1L);
        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testFindByCategoriaId() {
        when(productoRepository.findByCategoriaId(1L)).thenReturn(Arrays.asList(producto));
        when(inventarioService.consultarStockDisponible(1L)).thenReturn(100);

        List<Producto> resultados = productoService.findByCategoriaId(1L);

        assertFalse(resultados.isEmpty());
        assertEquals(100, resultados.get(0).getStockDisponible());
        verify(productoRepository, times(1)).findByCategoriaId(1L);
    }
}
