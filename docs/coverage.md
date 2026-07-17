# Cobertura de código con JaCoCo

Este proyecto usa [JaCoCo](https://www.jacoco.org/jacoco/) para medir la cobertura de tests unitarios e integración en ambos módulos Spring Boot, con un **reporte agregado** único.

## Comandos

Desde la raíz del repositorio (`minimarket-plus/`):

```bash
# Tests + reportes por módulo + reporte agregado
./mvnw clean verify
```

También puedes generar los reportes si ya ejecutaste los tests:

```bash
./mvnw verify
```

Para un módulo concreto (sin el agregado):

```bash
./mvnw -pl auth-service verify
./mvnw -pl minimarket-app verify
```

Solo el agregado (después de haber corrido tests en los servicios):

```bash
./mvnw -pl coverage-report -am verify
```

## Reportes

### Agregado (recomendado)

Tras `./mvnw clean verify`, abre el reporte unificado:

```
coverage-report/target/site/jacoco-aggregate/index.html
```

Incluye la cobertura de `auth-service` y `minimarket-app` en un solo HTML.

### Por módulo

Cada servicio también genera su propio reporte:

```
auth-service/target/site/jacoco/index.html
minimarket-app/target/site/jacoco/index.html
```

Los reportes muestran cobertura por paquete, clase y método (instrucciones, ramas y líneas).

## Configuración

El plugin `jacoco-maven-plugin` (v0.8.12) está definido en el parent `pom.xml` (`pluginManagement`) y activado en:

* `auth-service/pom.xml` — `prepare-agent` + `report`
* `minimarket-app/pom.xml` — `prepare-agent` + `report`
* `coverage-report/pom.xml` — `report-aggregate` (combina ambos)

Comportamiento:

- **prepare-agent**: instrumenta las clases antes de que Surefire corra los tests en cada servicio.
- **report**: genera el HTML por módulo en `target/site/jacoco/`.
- **report-aggregate**: genera el HTML combinado en `coverage-report/target/site/jacoco-aggregate/`.

### Exclusiones

Clases de arranque o seed de datos excluidas del reporte de cada módulo:

| Módulo | Exclusiones |
|--------|-------------|
| `auth-service` | `AuthServiceApplication`, `config/AuthDataInitializer` |
| `minimarket-app` | `MinimarketApplication`, `config/DataInitializer` |

## Dependencias de test

Los tests usan JUnit 5 y Mockito (incluidos en `spring-boot-starter-test`). Mockito se usa en tests unitarios de servicios donde se simulan repositorios y dependencias externas.
