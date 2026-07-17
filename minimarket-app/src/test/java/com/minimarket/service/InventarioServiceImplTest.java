package com.minimarket.service;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.impl.InventarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Inventario inventario;
    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche entera 1L");

        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setCantidad(50);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());
        inventario.setProducto(producto);
    }

    @Test
    void testFindAll() {
        when(inventarioRepository.findAll()).thenReturn(Arrays.asList(inventario));
        List<Inventario> resultados = inventarioService.findAll();
        assertEquals(1, resultados.size());
        verify(inventarioRepository, times(1)).findAll();
    }

    @Test
    void testFindById_Encontrado() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));
        Inventario resultado = inventarioService.findById(1L);
        assertNotNull(resultado);
        assertEquals(50, resultado.getCantidad());
    }

    @Test
    void testFindById_NoEncontrado() {
        when(inventarioRepository.findById(2L)).thenReturn(Optional.empty());
        Inventario resultado = inventarioService.findById(2L);
        assertNull(resultado);
    }

    @Test
    void testDeleteById() {
        doNothing().when(inventarioRepository).deleteById(1L);
        inventarioService.deleteById(1L);
        verify(inventarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void testFindByProductoId() {
        when(inventarioRepository.findByProductoId(1L)).thenReturn(Arrays.asList(inventario));
        List<Inventario> resultados = inventarioService.findByProductoId(1L);
        assertFalse(resultados.isEmpty());
        verify(inventarioRepository, times(1)).findByProductoId(1L);
    }

    @Test
    void testSave_Exito() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        Inventario resultado = inventarioService.save(inventario);

        assertNotNull(resultado);
        verify(inventarioRepository, times(1)).save(inventario);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("validacionesInvalidasDeSave")
    void save_datosInvalidos_lanzaIllegalArgument(Consumer<Inventario> mutador, String mensajeEsperado) {
        mutador.accept(inventario);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                inventarioService.save(inventario));

        assertEquals(mensajeEsperado, exception.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    private static Stream<Arguments> validacionesInvalidasDeSave() {
        return Stream.of(
                Arguments.of(
                        (Consumer<Inventario>) movimiento -> movimiento.setTipoMovimiento(null),
                        "El tipo de movimiento no puede ser nulo o vacío"),
                Arguments.of(
                        (Consumer<Inventario>) movimiento -> movimiento.setCantidad(null),
                        "La cantidad no puede ser nula o menor/igual a cero"),
                Arguments.of(
                        (Consumer<Inventario>) movimiento -> movimiento.setCantidad(0),
                        "La cantidad no puede ser nula o menor/igual a cero"),
                Arguments.of(
                        (Consumer<Inventario>) movimiento -> movimiento.setProducto(null),
                        "El producto asociado es nulo o inválido")
        );
    }

    @Test
    void testSave_ProductoNoExisteEnBD_LanzaExcepcion() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.save(inventario);
        });
        assertEquals("El producto asociado no existe en la base de datos", exception.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void consultarStockDisponible_sinMovimientos_retornaCero() {
        when(inventarioRepository.findByProductoId(1L)).thenReturn(List.of());

        int stockDisponible = inventarioService.consultarStockDisponible(1L);

        assertEquals(0, stockDisponible);
    }

    @Test
    void consultarStockDisponible_soloEntradas_sumaCantidades() {
        when(inventarioRepository.findByProductoId(1L)).thenReturn(List.of(movimiento("Entrada", 50)));

        int stockDisponible = inventarioService.consultarStockDisponible(1L);

        assertEquals(50, stockDisponible);
    }

    @Test
    void consultarStockDisponible_entradasYSalidas_calculaBalance() {
        when(inventarioRepository.findByProductoId(1L))
                .thenReturn(List.of(movimiento("Entrada", 50), movimiento("Salida", 12)));

        int stockDisponible = inventarioService.consultarStockDisponible(1L);

        assertEquals(38, stockDisponible);
    }

    @Test
    void registrarEntrada_productoExistente_creaMovimientoEntrada() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventarioService.registrarEntrada(1L, 30);

        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository).save(captor.capture());
        Inventario movimiento = captor.getValue();
        assertEquals("Entrada", movimiento.getTipoMovimiento());
        assertEquals(30, movimiento.getCantidad());
        assertEquals(producto, movimiento.getProducto());
        assertNotNull(movimiento.getFechaMovimiento());
    }

    @Test
    void registrarEntrada_cantidadInvalida_lanzaIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                inventarioService.registrarEntrada(1L, 0));

        assertEquals("La cantidad debe ser mayor a cero", exception.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void registrarEntrada_productoInexistente_lanzaIllegalArgumentException() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                inventarioService.registrarEntrada(99L, 5));

        assertEquals("Producto con id 99 no encontrado", exception.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void registrarSalida_stockSuficiente_creaMovimientoSalida() {
        when(inventarioRepository.findByProductoId(1L)).thenReturn(List.of(movimiento("Entrada", 50)));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inventarioService.registrarSalida(1L, 5);

        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioRepository).save(captor.capture());
        assertEquals("Salida", captor.getValue().getTipoMovimiento());
        assertEquals(5, captor.getValue().getCantidad());
    }

    @Test
    void registrarSalida_stockInsuficiente_lanzaIllegalStateException() {
        when(inventarioRepository.findByProductoId(1L)).thenReturn(List.of());
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                inventarioService.registrarSalida(1L, 5));

        assertEquals("Stock insuficiente para el producto", exception.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    private Inventario movimiento(String tipo, int cantidad) {
        Inventario movimiento = new Inventario();
        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setFechaMovimiento(new Date());
        return movimiento;
    }
}
