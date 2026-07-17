package com.minimarket.entity;

import com.minimarket.exception.InsufficientStockException;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrito> items = new ArrayList<>();

    protected Carrito() {
    }

    public Carrito(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public void agregarProducto(
            Long productoId,
            String nombreProducto,
            Double precio,
            int cantidad,
            int stockDisponible) {
        Optional<ItemCarrito> itemExistente = buscarItemPorProducto(productoId);
        if (itemExistente.isPresent()) {
            validarStockDisponible(nombreProducto, stockDisponible, itemExistente.get().getCantidad(), cantidad);
            itemExistente.get().incrementarCantidad(cantidad);
            return;
        }
        validarStockDisponible(nombreProducto, stockDisponible, 0, cantidad);
        items.add(new ItemCarrito(this, productoId, nombreProducto, precio, cantidad));
    }

    public void quitarProducto(Long productoId) {
        buscarItemPorProducto(productoId).ifPresent(items::remove);
    }

    public void vaciar() {
        items.clear();
    }

    private Optional<ItemCarrito> buscarItemPorProducto(Long productoId) {
        return items.stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst();
    }

    private void validarStockDisponible(
            String nombreProducto, int stockDisponible, int cantidadActual, int cantidadSolicitada) {
        int restante = stockDisponible - cantidadActual;
        if (cantidadSolicitada > restante) {
            throw new InsufficientStockException(nombreProducto, restante, cantidadSolicitada);
        }
    }
}
