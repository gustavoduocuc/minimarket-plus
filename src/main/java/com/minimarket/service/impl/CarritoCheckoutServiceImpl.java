package com.minimarket.service.impl;

import com.minimarket.entity.*;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.CarritoCheckoutService;
import com.minimarket.service.PaymentProcessor;
import com.minimarket.service.UsuarioService;
import com.minimarket.service.VentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.Objects.requireNonNull;

@Service
public class CarritoCheckoutServiceImpl implements CarritoCheckoutService {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final VentaService ventaService;
    private final PaymentProcessor paymentProcessor;

    public CarritoCheckoutServiceImpl(
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository,
            CarritoRepository carritoRepository,
            ProductoRepository productoRepository,
            VentaService ventaService,
            PaymentProcessor paymentProcessor) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.ventaService = ventaService;
        this.paymentProcessor = paymentProcessor;
    }

    @Override
    @Transactional
    public Venta checkout(String username, MetodoPago metodoPago) {
        Usuario usuario = usuarioService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return checkoutCarritoDe(usuario, metodoPago);
    }

    @Override
    @Transactional
    public Venta checkoutParaUsuario(Long usuarioId, MetodoPago metodoPago) {
        Long usuarioIdValido = requireNonNull(usuarioId, "Usuario inválido");
        Usuario usuario = usuarioRepository.findById(usuarioIdValido)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return checkoutCarritoDe(usuario, metodoPago);
    }

    private Venta checkoutCarritoDe(Usuario usuario, MetodoPago metodoPago) {
        if (metodoPago == null) {
            throw new IllegalArgumentException("El metodo de pago es obligatorio");
        }

        Carrito carrito = carritoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalStateException("No hay productos en el carrito"));

        if (carrito.getItems().isEmpty()) {
            throw new IllegalStateException("No hay productos en el carrito");
        }

        Venta venta = buildVenta(usuario, metodoPago, carrito.getItems());
        Venta ventaGuardada = ventaService.save(venta);
        paymentProcessor.initiatePayment(ventaGuardada);
        carritoRepository.delete(carrito);
        return ventaGuardada;
    }

    private Venta buildVenta(Usuario usuario, MetodoPago metodoPago, List<ItemCarrito> items) {
        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setMetodoPago(metodoPago);
        venta.setEstadoPago(EstadoPago.PENDIENTE_PAGO);

        List<DetalleVenta> detalles = new ArrayList<>();
        for (ItemCarrito item : items) {
            Producto productoEnCarrito = requireNonNull(item.getProducto(), "Producto inválido en el carrito");
            Long productoId = requireNonNull(productoEnCarrito.getId(), "Producto inválido en el carrito");
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto con id " + productoId + " not found"));

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecio(producto.getPrecio());
            detalles.add(detalle);
        }
        venta.setDetalles(detalles);
        return venta;
    }
}
