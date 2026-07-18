# Seguridad, acceso y datos locales

Documentación operativa de autenticación, políticas de acceso y bases de datos en el entorno local multi-módulo.

## Arquitectura de autenticación

| Servicio | Puerto | Rol |
|----------|--------|-----|
| `auth-service` | 8081 | Fuente de verdad de credenciales y roles: login, registro, gestión de usuarios y emisión de JWT |
| `catalogo-inventario-service` | 8082 | Valida JWT (claims `sub` + `roles`) para catálogo e inventario |
| `ventas-service` | 8080 | Valida JWT para ventas/carrito/notificaciones; proyección local de usuario (`id` + `username`) solo para FKs; descuenta stock en catálogo vía `POST /internal/inventario/salidas` con `X-Internal-Token` |

Los tres servicios deben usar el mismo `jwt.secret` (variable de entorno `JWT_SECRET` o valor por defecto en `application.properties`).

**Proyección de usuario en ventas:** `ventas-service` no guarda password ni roles. Tras registro/alta en auth, se hace un `POST /internal/usuarios` best-effort (header `X-Internal-Token`). Si ventas está caído, auth igual responde OK; al primer uso de carrito/checkout se crea la proyección con `ensure` lazy por username del JWT. Los IDs de usuario en ventas son locales (no necesariamente iguales a los de auth). Staff opera carritos ajenos según authorities del JWT, no según una entidad `Rol` local.

Propiedades: `ventas.base-url` / `ventas.internal-token` en auth; `ventas.internal-token` en ventas (mismo valor; en prod vía env `VENTAS_INTERNAL_TOKEN`). Descuento de stock: `catalogo.internal-token` en ventas y catálogo (env `CATALOGO_INTERNAL_TOKEN`).

El seed de demo crea usernames en ventas (sin password) y credenciales completas solo en auth. Categorías/productos/stock viven solo en `catalogo-inventario-service`.

## Política de contraseñas

Las contraseñas en registro y alta de usuarios (`auth-service`) deben cumplir:

* Mínimo 8 caracteres
* Al menos una mayúscula, una minúscula, un número y un carácter especial (`!@#$%^&*`, etc.)

## Protección del login (entorno local)

Aplica solo en `auth-service` sobre `POST /api/auth/login`:

* Bloqueo temporal tras intentos fallidos (en memoria; se reinicia al reiniciar la aplicación)
* Rate limiting por IP

Propiedades configurables en `auth-service/src/main/resources/application.properties` bajo `security.login.*`.

## Base de datos (entorno local)

Cada servicio tiene su propia H2 en memoria.

### auth-service (:8081)

* Consola H2 (perfil `dev`): http://localhost:8081/h2-console
* JDBC URL: `jdbc:h2:mem:authdb`
* Usuario: `sa`
* Contraseña: (vacía)

### catalogo-inventario-service (:8082)

* Consola H2 (perfil `dev`): http://localhost:8082/h2-console
* JDBC URL: `jdbc:h2:mem:catalogodb`
* Usuario: `sa`
* Contraseña: (vacía)

### ventas-service (:8080)

* Consola H2 (perfil `dev`): http://localhost:8080/h2-console
* JDBC URL: `jdbc:h2:mem:minimarketdb`
* Usuario: `sa`
* Contraseña: (vacía)

## Usuarios de prueba

| Usuario   | Contraseña     | Rol      |
|-----------|----------------|----------|
| admin     | Admin123!      | ADMIN    |
| gerente   | Gerente123!    | GERENTE  |
| empleado  | Empleado123!   | EMPLEADO |
| cliente   | Cliente123!    | CLIENTE  |

## Roles y permisos

### catalogo-inventario-service (:8082)

| Recurso | Público | CLIENTE | EMPLEADO | GERENTE | ADMIN |
|---------|---------|---------|----------|---------|-------|
| GET productos / categorías | Si | Si | Si | Si | Si |
| POST/PUT/DELETE productos | — | — | — | Si | Si |
| POST/PUT/DELETE categorías | — | — | — | Si | Si |
| GET inventario | — | — | Si | Si | Si |
| POST/PUT/DELETE inventario | — | — | — | Si | Si |
| POST `/api/inventario/salidas` | — | — | Si | Si | Si |

### ventas-service (:8080)

| Recurso | Público | CLIENTE | EMPLEADO | GERENTE | ADMIN |
|---------|---------|---------|----------|---------|-------|
| Carrito | — | Si | — | — | Si |
| Ventas / detalle ventas | — | — | Si | Si | Si |
| Notificaciones | — | Si | Si | Si | Si |

### auth-service (:8081)

| Recurso | Público | CLIENTE | EMPLEADO | GERENTE | ADMIN |
|---------|---------|---------|----------|---------|-------|
| Usuarios | — | — | — | — | Si |
| Login / registro | Si | Si | Si | Si | Si |

Notas:

* Staff (`EMPLEADO` / `GERENTE` / `ADMIN`) puede operar carritos de terceros y checkout asistido según las reglas de `SecurityConfig` en `ventas-service`.
* La gestión de usuarios (`/api/usuarios`) vive únicamente en `auth-service`.
* Si `catalogo-inventario-service` no responde al descontar stock, `ventas-service` responde **503** y **no** marca la venta como pagada.
* No hay API Gateway: el cliente usa tres puertos.
