package com.minimarket.service;

import com.minimarket.dto.ProductoMasVendidoResponse;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.impl.AnalisisVentasServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalisisVentasServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private AnalisisVentasServiceImpl analisisVentasService;

    @Test
    void productosMasVendidos_sinVentasPagadas_retornaRankingVacio() {
        when(ventaRepository.findByEstadoPago(EstadoPago.PAGADO)).thenReturn(List.of());

        List<ProductoMasVendidoResponse> ranking = analisisVentasService.productosMasVendidos();

        assertTrue(ranking.isEmpty());
        verify(ventaRepository).findByEstadoPago(EstadoPago.PAGADO);
    }

    @Test
    void productosMasVendidos_unaVentaPagadaConUnProducto_retornaEseProducto() {
        Producto arroz = new Producto();
        arroz.setId(1L);
        arroz.setNombre("Arroz 1kg");

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(arroz);
        detalle.setCantidad(3);

        Venta venta = new Venta();
        venta.setEstadoPago(EstadoPago.PAGADO);
        venta.setDetalles(List.of(detalle));

        when(ventaRepository.findByEstadoPago(EstadoPago.PAGADO)).thenReturn(List.of(venta));

        List<ProductoMasVendidoResponse> ranking = analisisVentasService.productosMasVendidos();

        assertEquals(1, ranking.size());
        assertEquals(1L, ranking.get(0).getProductoId());
        assertEquals("Arroz 1kg", ranking.get(0).getNombre());
        assertEquals(3, ranking.get(0).getCantidadVendida());
    }

    @Test
    void productosMasVendidos_variasVentasPagadasDelMismoProducto_sumaCantidades() {
        Producto arroz = new Producto();
        arroz.setId(1L);
        arroz.setNombre("Arroz 1kg");

        DetalleVenta detalle1 = new DetalleVenta();
        detalle1.setProducto(arroz);
        detalle1.setCantidad(3);

        DetalleVenta detalle2 = new DetalleVenta();
        detalle2.setProducto(arroz);
        detalle2.setCantidad(5);

        Venta venta1 = new Venta();
        venta1.setEstadoPago(EstadoPago.PAGADO);
        venta1.setDetalles(List.of(detalle1));

        Venta venta2 = new Venta();
        venta2.setEstadoPago(EstadoPago.PAGADO);
        venta2.setDetalles(List.of(detalle2));

        when(ventaRepository.findByEstadoPago(EstadoPago.PAGADO)).thenReturn(List.of(venta1, venta2));

        List<ProductoMasVendidoResponse> ranking = analisisVentasService.productosMasVendidos();

        assertEquals(1, ranking.size());
        assertEquals(8, ranking.get(0).getCantidadVendida());
    }

    @Test
    void productosMasVendidos_variosProductos_ordenaPorCantidadDescendente() {
        Producto arroz = new Producto();
        arroz.setId(1L);
        arroz.setNombre("Arroz 1kg");

        Producto leche = new Producto();
        leche.setId(2L);
        leche.setNombre("Leche 1L");

        DetalleVenta detalleArroz = new DetalleVenta();
        detalleArroz.setProducto(arroz);
        detalleArroz.setCantidad(5);

        DetalleVenta detalleLeche = new DetalleVenta();
        detalleLeche.setProducto(leche);
        detalleLeche.setCantidad(10);

        Venta venta = new Venta();
        venta.setEstadoPago(EstadoPago.PAGADO);
        venta.setDetalles(List.of(detalleArroz, detalleLeche));

        when(ventaRepository.findByEstadoPago(EstadoPago.PAGADO)).thenReturn(List.of(venta));

        List<ProductoMasVendidoResponse> ranking = analisisVentasService.productosMasVendidos();

        assertAll(
                () -> assertEquals(2, ranking.size()),
                () -> assertEquals(2L, ranking.get(0).getProductoId()),
                () -> assertEquals(10, ranking.get(0).getCantidadVendida()),
                () -> assertEquals(1L, ranking.get(1).getProductoId()),
                () -> assertEquals(5, ranking.get(1).getCantidadVendida())
        );
    }

    @Test
    void productosMasVendidos_soloConsultaVentasPagadas() {
        when(ventaRepository.findByEstadoPago(EstadoPago.PAGADO)).thenReturn(List.of());

        analisisVentasService.productosMasVendidos();

        verify(ventaRepository).findByEstadoPago(EstadoPago.PAGADO);
        verify(ventaRepository, never()).findByEstadoPago(EstadoPago.PENDIENTE_PAGO);
        verify(ventaRepository, never()).findAll();
    }
}
