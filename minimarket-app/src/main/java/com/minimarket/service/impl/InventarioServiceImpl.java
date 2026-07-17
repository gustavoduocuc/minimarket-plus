package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.inventario.TipoMovimientoInventario;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.InventarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;

    public InventarioServiceImpl(InventarioRepository inventarioRepository, ProductoRepository productoRepository) {
        this.inventarioRepository = inventarioRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    @Override
    public Inventario findById(Long id) {
        return inventarioRepository.findById(requireNonNull(id)).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        inventarioRepository.deleteById(requireNonNull(id));
    }

    @Override
    public List<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    @Override
    public Inventario save(Inventario inventario) {
        if (inventario.getTipoMovimiento() == null || inventario.getTipoMovimiento().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de movimiento no puede ser nulo o vacío");
        }
        if (inventario.getCantidad() == null || inventario.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad no puede ser nula o menor/igual a cero");
        }
        if (inventario.getProducto() == null || inventario.getProducto().getId() == null) {
            throw new IllegalArgumentException("El producto asociado es nulo o inválido");
        }
        Long productoId = requireNonNull(inventario.getProducto().getId(), "El producto asociado es nulo o inválido");
        productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("El producto asociado no existe en la base de datos"));

        return inventarioRepository.save(inventario);
    }

    @Override
    public int consultarStockDisponible(Long productoId) {
        Long productoIdValido = requireNonNull(productoId, "Producto inválido");
        List<Inventario> movimientos = inventarioRepository.findByProductoId(productoIdValido);
        return calcularBalance(movimientos);
    }

    @Override
    @Transactional
    public void registrarEntrada(Long productoId, int cantidad) {
        validarCantidadPositiva(cantidad);
        Producto producto = buscarProducto(productoId);
        guardarMovimiento(producto, cantidad, TipoMovimientoInventario.entrada);
    }

    @Override
    @Transactional
    public void registrarSalida(Long productoId, int cantidad) {
        validarCantidadPositiva(cantidad);
        Producto producto = buscarProducto(productoId);
        int stockDisponible = consultarStockDisponible(productoId);
        if (cantidad > stockDisponible) {
            throw new IllegalStateException("Stock insuficiente para el producto");
        }
        guardarMovimiento(producto, cantidad, TipoMovimientoInventario.salida);
    }

    // El stock se recalcula sumando todos los movimientos en cada consulta.
    // Para un minimarket pequeño es suficiente, pero a gran escala (muchos productos
    // o historial extenso) este cálculo se vuelve costoso.
    // Mejora futura: mantener un saldo persistido por producto o snapshots periódicos
    // del balance para no recorrer el historial completo en cada lectura.
    private int calcularBalance(List<Inventario> movimientos) {
        int entradas = movimientos.stream()
                .filter(movimiento -> TipoMovimientoInventario.entrada.equals(movimiento.getTipoMovimiento()))
                .mapToInt(movimiento -> requireNonNull(movimiento.getCantidad(), "Cantidad inválida"))
                .sum();
        int salidas = movimientos.stream()
                .filter(movimiento -> TipoMovimientoInventario.salida.equals(movimiento.getTipoMovimiento()))
                .mapToInt(movimiento -> requireNonNull(movimiento.getCantidad(), "Cantidad inválida"))
                .sum();
        return entradas - salidas;
    }

    private void validarCantidadPositiva(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }

    private Producto buscarProducto(Long productoId) {
        return productoRepository.findById(requireNonNull(productoId, "Producto inválido"))
                .orElseThrow(() -> new IllegalArgumentException("Producto con id " + productoId + " no encontrado"));
    }

    private void guardarMovimiento(Producto producto, int cantidad, String tipoMovimiento) {
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setCantidad(cantidad);
        inventario.setTipoMovimiento(tipoMovimiento);
        inventario.setFechaMovimiento(new Date());
        inventarioRepository.save(inventario);
    }
}
