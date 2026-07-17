package com.minimarket.openapi;

/**
 * Ejemplos JSON HAL para documentación OpenAPI (solo docs, no runtime).
 */
public final class HalExamples {

    private HalExamples() {
    }

    public static final String CARRITO_RESOURCE = """
            {
              "id": 1,
              "usuario": {"id": 4, "username": "cliente"},
              "items": [{"id": 1, "productoId": 1, "nombreProducto": "Leche entera 1L", "precio": 1200.0, "cantidad": 2}],
              "_links": {
                "self": {"href": "http://localhost:8080/api/carrito"},
                "carrito": {"href": "http://localhost:8080/api/carrito"},
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

    public static final String VENTA_RESOURCE = """
            {
              "id": 1,
              "usuario": {"id": 4, "username": "cliente"},
              "fecha": "2026-07-09T12:00:00.000+00:00",
              "detalles": [{"productoId": 1, "nombreProducto": "Leche entera 1L", "precio": 1200.0, "cantidad": 2}],
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
