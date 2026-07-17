package com.minimarket.service;

import com.minimarket.catalogo.Producto;

public interface ProductoService {
    Producto findById(Long id);

    int consultarStock(Long productoId);
}
