package com.minimarket.service;

import com.minimarket.entity.MetodoPago;
import com.minimarket.entity.TipoEntrega;
import com.minimarket.entity.Venta;

public interface CarritoCheckoutService {
    Venta checkout(String username, MetodoPago metodoPago, TipoEntrega tipoEntrega);

    Venta checkoutParaUsuario(Long usuarioId, MetodoPago metodoPago, TipoEntrega tipoEntrega);
}
