# Seguridad, acceso y datos locales

Documentación operativa de autenticación, políticas de acceso y bases de datos en el entorno local multi-módulo.

## Arquitectura de autenticación

| Servicio | Puerto | Rol |
|----------|--------|-----|
| `auth-service` | 8081 | Fuente de verdad de credenciales: login, registro, gestión de usuarios y emisión de JWT |
| `minimarket-app` | 8080 | Valida JWT (claims `sub` + `roles`) para proteger catálogo, inventario, ventas y notificaciones |

Ambos servicios deben usar el mismo `jwt.secret` (variable de entorno `JWT_SECRET` o valor por defecto en `application.properties`).

Los usuarios seed se replican en ambas bases H2 en memoria para que carrito/ventas puedan referenciar usuarios locales sin sincronización en runtime.

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

### minimarket-app (:8080)

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

| Recurso | Público | CLIENTE | EMPLEADO | GERENTE | ADMIN |
|---------|---------|---------|----------|---------|-------|
| GET productos / categorías | Si | Si | Si | Si | Si |
| POST/PUT/DELETE productos | — | — | — | Si | Si |
| POST/PUT/DELETE categorías | — | — | — | Si | Si |
| Carrito | — | Si | — | — | Si |
| GET inventario | — | — | Si | Si | Si |
| POST/PUT/DELETE inventario | — | — | — | Si | Si |
| Ventas / detalle ventas | — | — | Si | Si | Si |
| Usuarios (`auth-service`) | — | — | — | — | Si |
| Login / registro (`auth-service`) | Si | Si | Si | Si | Si |
| /public/** | Si | Si | Si | Si | Si |

Notas:

* Staff (`EMPLEADO` / `GERENTE` / `ADMIN`) puede operar carritos de terceros y checkout asistido según las reglas de `SecurityConfig` en `minimarket-app`.
* La gestión de usuarios (`/api/usuarios`) vive únicamente en `auth-service`.
