package com.minimarket.service;

import com.minimarket.entity.Carrito;

import java.util.List;
import java.util.Optional;

public interface CarritoService {
    Carrito agregarProducto(String callerUsername, Long usuarioObjetivoId, Long productoId, int cantidad);
    Optional<Carrito> obtenerCarritoDe(String username);
    List<Carrito> findAll();
    Optional<Carrito> findById(Long id);
    void quitarProducto(String username, Long productoId);
    void vaciarCarrito(String username);
}
