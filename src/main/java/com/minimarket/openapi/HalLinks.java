package com.minimarket.openapi;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "HalLinks",
        description = """
                Enlaces HATEOAS (HAL). Objeto dinámico rel -> { href, templated? }. \
                Rels frecuentes: self, productos, stock, inventario, carrito, usuarios, ventas, \
                crearProducto, actualizarProducto, eliminarProducto, agregarProducto, checkout, \
                registrarMovimiento, crearUsuario, confirmarPago, etc. \
                Los rels de mutación solo aparecen según el rol del JWT.""",
        additionalPropertiesSchema = HalLink.class)
public class HalLinks {
}
