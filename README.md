# MINIMARKET PLUS

Backend REST multi-módulo para la gestión de un minimarket, desarrollado con Spring Boot 3 y Spring Security (arquitectura stateless con JWT).

| Módulo | Puerto | Responsabilidad |
|--------|--------|-----------------|
| `auth-service` | 8081 | Login, registro, usuarios, emisión JWT |
| `catalogo-inventario-service` | 8082 | Productos, categorías, inventario (stock) |
| `ventas-service` | 8080 | Ventas, carrito, notificaciones (valida JWT; consume catálogo vía RestClient) |

Sin API Gateway: el cliente apunta a cada puerto.

## Requisitos

* Java 17+
* Maven (incluido via `./mvnw`)

## Ejecución local

Terminal 1 — autenticación:
```bash
./mvnw -pl auth-service spring-boot:run
```

Terminal 2 — catálogo e inventario:
```bash
./mvnw -pl catalogo-inventario-service spring-boot:run
```

Terminal 3 — ventas:
```bash
./mvnw -pl ventas-service spring-boot:run
```

Los tres usan el mismo `JWT_SECRET` (valor por defecto en cada `application.properties`).
`ventas-service` apunta a catálogo con `catalogo.base-url` (por defecto `http://localhost:8082`).
Auth sincroniza proyecciones de usuario hacia ventas con `ventas.base-url` y `ventas.internal-token` (mismo token en ambos; env `VENTAS_INTERNAL_TOKEN` en prod).

### OpenAPI / Swagger

* Auth: http://localhost:8081/swagger-ui.html
* Catálogo: http://localhost:8082/swagger-ui.html
* Ventas: http://localhost:8080/swagger-ui.html

## Flujo E2E (curl)

**1. Login (`auth-service`):**

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"cliente", "password":"Cliente123!"}'
```

Guarda el `token` de la respuesta.

**2. Listar productos (`catalogo-inventario-service`):**

```bash
curl -X GET http://localhost:8082/api/productos
```

**3. Agregar al carrito (`ventas-service`):**

```bash
curl -X POST http://localhost:8080/api/carrito \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"producto":{"id":1},"cantidad":1}'
```

**4. Checkout:**

```bash
curl -X POST http://localhost:8080/api/carrito/checkout \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"metodoPago":"EFECTIVO"}'
```

**5. Confirmar pago (staff — login como `empleado`):**

```bash
curl -X POST http://localhost:8080/api/ventas/<VENTA_ID>/confirmar-pago \
  -H "Authorization: Bearer <TOKEN_EMPLEADO>"
```

## Seguridad, acceso y datos

Política de contraseñas, protección del login, bases H2, usuarios de prueba y matriz de roles/permisos:

→ [docs/seguridad-acceso-y-datos.md](docs/seguridad-acceso-y-datos.md)

## Colección Postman

Importar en Postman:

[`docs/MiniMarket Plus API.postman_collection.json`](docs/MiniMarket%20Plus%20API.postman_collection.json)

Variables de la colección:

| Variable | Valor por defecto | Uso |
|----------|-------------------|-----|
| `auth_base_url` | `http://localhost:8081` | Login, registro, usuarios |
| `catalogo_base_url` | `http://localhost:8082` | Productos, categorías, inventario |
| `base_url` | `http://localhost:8080` | Carrito, ventas, notificaciones |
| `token` | (se llena al hacer Login) | Bearer Token de la colección |

## Tests

```bash
./mvnw clean test
```

## Cobertura

Visita [docs/coverage.md](docs/coverage.md) para ver más detalle.

Reporte agregado (auth + catálogo + ventas):

```bash
./mvnw clean verify
```

Abrir: `coverage-report/target/site/jacoco-aggregate/index.html`
