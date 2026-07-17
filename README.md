# MINIMARKET PLUS

Backend REST multi-módulo para la gestión de un minimarket, desarrollado con Spring Boot 3 y Spring Security (arquitectura stateless con JWT).

| Módulo | Puerto | Responsabilidad |
|--------|--------|-----------------|
| `auth-service` | 8081 | Login, registro, usuarios, emisión JWT |
| `minimarket-app` | 8080 | Catálogo, inventario, ventas y notificaciones (valida JWT) |

## Requisitos

* Java 17+
* Maven (incluido via `./mvnw`)

## Ejecución local

Terminal 1 — autenticación:
```bash
./mvnw -pl auth-service spring-boot:run
```

Terminal 2 — aplicación de negocio:
```bash
./mvnw -pl minimarket-app spring-boot:run
```

Ambos usan el mismo `JWT_SECRET` (valor por defecto en cada `application.properties`).

### OpenAPI / Swagger

* Auth: http://localhost:8081/swagger-ui.html
* App: http://localhost:8080/swagger-ui.html

## Autenticación rápida (JWT)

Para rutas protegidas de `minimarket-app`, enviar `Authorization: Bearer <token>`.

**Login (`auth-service`):**

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin", "password":"Admin123!"}'
```

**Registro (`auth-service`):**

```bash
curl -X POST http://localhost:8081/api/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"username":"nuevo", "password":"Password123!"}'
```

**Endpoint protegido (`minimarket-app`):**

```bash
curl -X GET http://localhost:8080/api/ventas \
  -H "Authorization: Bearer <TU_TOKEN_AQUI>"
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
| `base_url` | `http://localhost:8080` | Catálogo, carrito, inventario, ventas, etc. |
| `token` | (se llena al hacer Login) | Bearer Token de la colección |

## Tests

```bash
./mvnw clean test
```

## Cobertura

Visita [docs/coverage.md](docs/coverage.md) para ver más detalle.

Reporte agregado (ambos servicios):

```bash
./mvnw clean verify
```

Abrir: `coverage-report/target/site/jacoco-aggregate/index.html`
