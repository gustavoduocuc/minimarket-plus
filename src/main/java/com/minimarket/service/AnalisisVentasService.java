package com.minimarket.service;

import com.minimarket.dto.ProductoMasVendidoResponse;

import java.util.List;

public interface AnalisisVentasService {

    List<ProductoMasVendidoResponse> productosMasVendidos();
}
