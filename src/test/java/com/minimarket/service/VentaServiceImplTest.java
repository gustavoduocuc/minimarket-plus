package com.minimarket.service;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Venta venta;
    private DetalleVenta detalle;
    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Café");
        producto.setStock(20);

        detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(5);

        venta = new Venta();
        venta.setId(1L);
        venta.setDetalles(Arrays.asList(detalle));
    }

    @Test
    void testFindAll() {
        when(ventaRepository.findAll()).thenReturn(Arrays.asList(venta));
        List<Venta> resultados = ventaService.findAll();
        assertEquals(1, resultados.size());
        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    void testFindById_IdNulo_RetornaNull() {
        Venta resultado = ventaService.findById(null);
        assertNull(resultado);
        verify(ventaRepository, never()).findById(anyLong());
    }

    @Test
    void testFindById_Encontrado() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));
        Venta resultado = ventaService.findById(1L);
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void testSave_VentaNula_LanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> {
            ventaService.save(null);
        });
    }

    @Test
    void testSave_VentaSinDetalles_GuardaDirecto() {
        venta.setDetalles(null);
        when(ventaRepository.save(venta)).thenReturn(venta);
        
        Venta resultado = ventaService.save(venta);
        
        assertNotNull(resultado);
        verify(productoRepository, never()).findById(anyLong());
        verify(ventaRepository, times(1)).save(venta);
    }

    @Test
    void testSave_ProductoNuloEnDetalle_LanzaExcepcion() {
        detalle.setProducto(null);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ventaService.save(venta);
        });
        assertEquals("Producto no especificado en detalle de venta", exception.getMessage());
    }

    @Test
    void testSave_ProductoIdNuloEnDetalle_LanzaExcepcion() {
        producto.setId(null);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ventaService.save(venta);
        });
        assertEquals("Producto no especificado en detalle de venta", exception.getMessage());
    }

    @Test
    void testSave_ProductoNoEncontradoEnBD_LanzaExcepcion() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ventaService.save(venta);
        });
        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void testSave_StockInsuficiente_LanzaExcepcion() {
        detalle.setCantidad(50);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> {
            ventaService.save(venta);
        });
        assertEquals("Café", exception.getProducto());
        assertEquals(20, exception.getDisponible());
        assertEquals(50, exception.getSolicitado());
        assertTrue(exception.getClientMessage().contains("Solo quedan 20 unidades"));

        verify(productoRepository, never()).save(any(Producto.class));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void testSave_Exito_DescuentaStockYGuardaVenta() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(ventaRepository.save(venta)).thenReturn(venta);
        
        Venta resultado = ventaService.save(venta);
        
        assertNotNull(resultado);
        assertEquals(15, producto.getStock()); 
        
        verify(productoRepository, times(1)).save(producto);
        verify(ventaRepository, times(1)).save(venta);
    }

    @Test
    void testFindByUsuarioId() {
        when(ventaRepository.findByUsuarioId(10L)).thenReturn(Arrays.asList(venta));
        List<Venta> resultados = ventaService.findByUsuarioId(10L);
        assertFalse(resultados.isEmpty());
        verify(ventaRepository, times(1)).findByUsuarioId(10L);
    }
}