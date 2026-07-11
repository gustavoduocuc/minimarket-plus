package com.minimarket.openapi;

/**
 * Ejemplos JSON HAL para documentación OpenAPI (solo docs, no runtime).
 */
public final class HalExamples {

    private HalExamples() {
    }

    public static final String PRODUCTO_RESOURCE = """
            {
              "id": 1,
              "nombre": "Leche entera 1L",
              "precio": 1200.0,
              "stockDisponible": 50,
              "categoria": {"id": 2, "nombre": "Lacteos"},
              "_links": {
                "self": {"href": "http://localhost:8080/api/productos/1"},
                "productos": {"href": "http://localhost:8080/api/productos"},
                "stock": {"href": "http://localhost:8080/api/productos/1/stock"}
              }
            }
            """;

    public static final String PRODUCTO_COLLECTION = """
            {
              "_embedded": {
                "productoList": [
                  {
                    "id": 1,
                    "nombre": "Leche entera 1L",
                    "precio": 1200.0,
                    "stockDisponible": 50,
                    "categoria": {"id": 2, "nombre": "Lacteos"},
                    "_links": {
                      "self": {"href": "http://localhost:8080/api/productos/1"},
                      "productos": {"href": "http://localhost:8080/api/productos"},
                      "stock": {"href": "http://localhost:8080/api/productos/1/stock"}
                    }
                  }
                ]
              },
              "_links": {
                "self": {"href": "http://localhost:8080/api/productos"}
              }
            }
            """;

    public static final String CARRITO_RESOURCE = """
            {
              "id": 1,
              "usuario": {"id": 4, "username": "cliente"},
              "items": [{"id": 1, "cantidad": 2, "producto": {"id": 1, "nombre": "Leche entera 1L"}}],
              "_links": {
                "self": {"href": "http://localhost:8080/api/carrito"},
                "carrito": {"href": "http://localhost:8080/api/carrito"},
                "productos": {"href": "http://localhost:8080/api/productos"},
                "agregarProducto": {"href": "http://localhost:8080/api/carrito"},
                "checkout": {"href": "http://localhost:8080/api/carrito/checkout"}
              }
            }
            """;

    public static final String CARRITO_COLLECTION = """
            {
              "_embedded": {
                "carritoList": [
                  {
                    "id": 1,
                    "usuario": {"id": 4, "username": "cliente"},
                    "items": [],
                    "_links": {
                      "self": {"href": "http://localhost:8080/api/carrito/1"},
                      "carrito": {"href": "http://localhost:8080/api/carrito/todos"}
                    }
                  }
                ]
              },
              "_links": {
                "self": {"href": "http://localhost:8080/api/carrito/todos"},
                "carrito": {"href": "http://localhost:8080/api/carrito/todos"}
              }
            }
            """;

    public static final String INVENTARIO_RESOURCE = """
            {
              "id": 1,
              "producto": {"id": 1, "nombre": "Leche entera 1L", "precio": 1200.0},
              "cantidad": 50,
              "tipoMovimiento": "Entrada",
              "fechaMovimiento": "2026-07-09T01:47:02.826+00:00",
              "_links": {
                "self": {"href": "http://localhost:8080/api/inventario/1"},
                "inventario": {"href": "http://localhost:8080/api/inventario"},
                "producto": {"href": "http://localhost:8080/api/productos/1"}
              }
            }
            """;

    public static final String INVENTARIO_COLLECTION = """
            {
              "_embedded": {
                "inventarioList": [
                  {
                    "id": 1,
                    "producto": {"id": 1, "nombre": "Leche entera 1L"},
                    "cantidad": 50,
                    "tipoMovimiento": "Entrada",
                    "_links": {
                      "self": {"href": "http://localhost:8080/api/inventario/1"},
                      "inventario": {"href": "http://localhost:8080/api/inventario"},
                      "producto": {"href": "http://localhost:8080/api/productos/1"}
                    }
                  }
                ]
              },
              "_links": {
                "self": {"href": "http://localhost:8080/api/inventario"},
                "inventario": {"href": "http://localhost:8080/api/inventario"}
              }
            }
            """;

    public static final String USUARIO_RESOURCE = """
            {
              "id": 1,
              "username": "admin",
              "roles": ["ADMIN"],
              "_links": {
                "self": {"href": "http://localhost:8080/api/usuarios/1"},
                "usuarios": {"href": "http://localhost:8080/api/usuarios"},
                "crearUsuario": {"href": "http://localhost:8080/api/usuarios"},
                "actualizarUsuario": {"href": "http://localhost:8080/api/usuarios/1"},
                "eliminarUsuario": {"href": "http://localhost:8080/api/usuarios/1"}
              }
            }
            """;

    public static final String USUARIO_COLLECTION = """
            {
              "_embedded": {
                "usuarioResponseDtoList": [
                  {
                    "id": 1,
                    "username": "admin",
                    "roles": ["ADMIN"],
                    "_links": {
                      "self": {"href": "http://localhost:8080/api/usuarios/1"},
                      "usuarios": {"href": "http://localhost:8080/api/usuarios"}
                    }
                  }
                ]
              },
              "_links": {
                "self": {"href": "http://localhost:8080/api/usuarios"},
                "usuarios": {"href": "http://localhost:8080/api/usuarios"},
                "crearUsuario": {"href": "http://localhost:8080/api/usuarios"}
              }
            }
            """;

    public static final String VENTA_RESOURCE = """
            {
              "id": 1,
              "usuario": {"id": 4, "username": "cliente"},
              "fecha": "2026-07-09T12:00:00.000+00:00",
              "detalles": [],
              "metodoPago": "EFECTIVO",
              "estadoPago": "PENDIENTE_PAGO",
              "_links": {
                "self": {"href": "http://localhost:8080/api/ventas/1"},
                "ventas": {"href": "http://localhost:8080/api/ventas"},
                "ventasPendientes": {"href": "http://localhost:8080/api/ventas/pendientes"},
                "confirmarPago": {"href": "http://localhost:8080/api/ventas/1/confirmar-pago"}
              }
            }
            """;

    public static final String VENTA_COLLECTION = """
            {
              "_embedded": {
                "ventaList": [
                  {
                    "id": 1,
                    "metodoPago": "EFECTIVO",
                    "estadoPago": "PENDIENTE_PAGO",
                    "_links": {
                      "self": {"href": "http://localhost:8080/api/ventas/1"},
                      "ventas": {"href": "http://localhost:8080/api/ventas"}
                    }
                  }
                ]
              },
              "_links": {
                "self": {"href": "http://localhost:8080/api/ventas"},
                "ventas": {"href": "http://localhost:8080/api/ventas"},
                "ventasPendientes": {"href": "http://localhost:8080/api/ventas/pendientes"}
              }
            }
            """;
}
