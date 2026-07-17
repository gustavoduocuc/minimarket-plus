package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.EstadoPago;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.InventarioService;
import com.minimarket.service.NotificacionService;
import com.minimarket.service.VentaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;
    private final NotificacionService notificacionService;

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            InventarioService inventarioService,
            NotificacionService notificacionService) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.inventarioService = inventarioService;
        this.notificacionService = notificacionService;
    }

    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta findById(Long id) {
        if (id == null) {
            return null;
        }
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public Venta save(Venta venta) {
        Venta nonNullVenta = Objects.requireNonNull(venta, "La venta es obligatoria");
        validateAndDeductStock(nonNullVenta);
        return ventaRepository.save(nonNullVenta);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public List<Venta> findPendientesDePago() {
        return ventaRepository.findByEstadoPago(EstadoPago.PENDIENTE_PAGO);
    }

    @Override
    public Venta confirmarPago(Long ventaId) {
        Venta venta = findById(ventaId);
        if (venta == null) {
            throw new IllegalArgumentException("Venta con id " + ventaId + " no encontrado");
        }
        if (venta.getEstadoPago() == EstadoPago.PAGADO) {
            throw new IllegalStateException("La venta ya fue pagada");
        }
        venta.setEstadoPago(EstadoPago.PAGADO);
        Venta ventaPagada = ventaRepository.save(venta);
        notificacionService.notificarCambioPedido(
                ventaPagada,
                "Pedido #" + ventaPagada.getId() + " pagado");
        return ventaPagada;
    }

    private void validateAndDeductStock(Venta venta) {
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            return;
        }
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = findProductoForDetalle(detalle);
            int stockDisponible = inventarioService.consultarStockDisponible(producto.getId());
            if (stockDisponible < detalle.getCantidad()) {
                throw new InsufficientStockException(
                        producto.getNombre(),
                        stockDisponible,
                        detalle.getCantidad());
            }
        }
        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = findProductoForDetalle(detalle);
            inventarioService.registrarSalida(producto.getId(), detalle.getCantidad());
            detalle.setProducto(producto);
        }
    }

    private Producto findProductoForDetalle(DetalleVenta detalle) {
        Producto productoRef = detalle.getProducto();
        if (productoRef == null) {
            throw new IllegalArgumentException("Producto no especificado en detalle de venta");
        }
        Long productoId = productoRef.getId();
        if (productoId == null) {
            throw new IllegalArgumentException("Producto no especificado en detalle de venta");
        }
        return productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Producto con id " + productoId + " no encontrado"));
    }
}
