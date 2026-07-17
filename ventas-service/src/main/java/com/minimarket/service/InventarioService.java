package com.minimarket.service;

public interface InventarioService {
    int consultarStockDisponible(Long productoId);

    void registrarSalida(Long productoId, int cantidad);
}
