package com.minimarket.service.impl;

import com.minimarket.dto.ProductoMasVendidoResponse;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.EstadoPago;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.AnalisisVentasService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalisisVentasServiceImpl implements AnalisisVentasService {

    private final VentaRepository ventaRepository;

    public AnalisisVentasServiceImpl(VentaRepository ventaRepository) {
        this.ventaRepository = ventaRepository;
    }

    @Override
    public List<ProductoMasVendidoResponse> productosMasVendidos() {
        return ventaRepository.findByEstadoPago(EstadoPago.PAGADO).stream()
                .filter(venta -> venta.getDetalles() != null)
                .flatMap(venta -> venta.getDetalles().stream())
                .collect(Collectors.groupingBy(
                        DetalleVenta::getProductoId,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                detalles -> new ProductoMasVendidoResponse(
                                        detalles.get(0).getProductoId(),
                                        detalles.get(0).getNombreProducto(),
                                        detalles.stream().mapToInt(DetalleVenta::getCantidad).sum()))))
                .values().stream()
                .sorted(Comparator.comparingInt(ProductoMasVendidoResponse::getCantidadVendida).reversed())
                .toList();
    }
}
