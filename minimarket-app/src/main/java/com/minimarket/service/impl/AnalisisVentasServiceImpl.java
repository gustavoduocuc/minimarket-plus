package com.minimarket.service.impl;

import com.minimarket.dto.ProductoMasVendidoResponse;
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
                        detalle -> detalle.getProducto(),
                        Collectors.summingInt(detalle -> detalle.getCantidad())))
                .entrySet().stream()
                .map(entry -> new ProductoMasVendidoResponse(
                        entry.getKey().getId(),
                        entry.getKey().getNombre(),
                        entry.getValue()))
                .sorted(Comparator.comparingInt(
                        (ProductoMasVendidoResponse response) -> response.getCantidadVendida()).reversed())
                .toList();
    }
}
