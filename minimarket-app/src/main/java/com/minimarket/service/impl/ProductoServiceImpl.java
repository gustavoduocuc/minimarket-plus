package com.minimarket.service.impl;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.exception.InvalidRequestException;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.InventarioService;
import com.minimarket.service.ProductoService;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final InventarioService inventarioService;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            InventarioService inventarioService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.inventarioService = inventarioService;
    }

    @Override
    public List<Producto> findAll() {
        List<Producto> productos = productoRepository.findAll();
        productos.forEach(this::enriquecerConStockDisponible);
        return productos;
    }

    @Override
    public Producto findById(Long id) {
        Producto producto = productoRepository.findById(requireNonNull(id)).orElse(null);
        if (producto != null) {
            enriquecerConStockDisponible(producto);
        }
        return producto;
    }

    @Override
    public Producto save(Producto producto) {
        Producto productoValido = requireNonNull(producto);
        resolveCategoria(productoValido);
        Producto guardado = productoRepository.save(productoValido);
        enriquecerConStockDisponible(guardado);
        return guardado;
    }

    @Override
    public void deleteById(Long id) {
        productoRepository.deleteById(id);
    }

    @Override
    public List<Producto> findByCategoriaId(Long categoriaId) {
        List<Producto> productos = productoRepository.findByCategoriaId(categoriaId);
        productos.forEach(this::enriquecerConStockDisponible);
        return productos;
    }

    @Override
    public int consultarStock(Long productoId) {
        Long productoIdValido = requireNonNull(productoId);
        if (productoRepository.findById(productoIdValido).isEmpty()) {
            throw new InvalidRequestException("Producto no encontrado", "Producto no encontrado");
        }
        return inventarioService.consultarStockDisponible(productoIdValido);
    }

    private void enriquecerConStockDisponible(Producto producto) {
        producto.setStockDisponible(inventarioService.consultarStockDisponible(producto.getId()));
    }

    private void resolveCategoria(Producto producto) {
        if (producto.getCategoria() == null || producto.getCategoria().getId() == null) {
            throw new InvalidRequestException("Invalid request", "Producto sin categoría");
        }

        Long categoriaId = requireNonNull(producto.getCategoria().getId());
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new InvalidRequestException(
                        "Categoría no válida",
                        "Categoría no encontrada: id=" + categoriaId));

        producto.setCategoria(categoria);
    }
}
