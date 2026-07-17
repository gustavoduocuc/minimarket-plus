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

    public void agregarProducto(Producto producto, int cantidad, int stockDisponible) {
        Optional<ItemCarrito> itemExistente = buscarItemPorProducto(producto.getId());
        if (itemExistente.isPresent()) {
            validarStockDisponible(producto, stockDisponible, itemExistente.get().getCantidad(), cantidad);
            itemExistente.get().incrementarCantidad(cantidad);
            return;
        }
        validarStockDisponible(producto, stockDisponible, 0, cantidad);
        items.add(new ItemCarrito(this, producto, cantidad));
    }

    public void quitarProducto(Long productoId) {
        buscarItemPorProducto(productoId).ifPresent(items::remove);
    }

    public void vaciar() {
        items.clear();
    }

    private Optional<ItemCarrito> buscarItemPorProducto(Long productoId) {
        return items.stream()
                .filter(item -> item.getProducto().getId().equals(productoId))
                .findFirst();
    }

    private void validarStockDisponible(Producto producto, int stockDisponible, int cantidadActual, int cantidadSolicitada) {
        int restante = stockDisponible - cantidadActual;
        if (cantidadSolicitada > restante) {
            throw new InsufficientStockException(producto.getNombre(), restante, cantidadSolicitada);
        }
    }
}
