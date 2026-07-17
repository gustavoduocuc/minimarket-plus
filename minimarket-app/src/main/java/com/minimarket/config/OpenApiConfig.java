package com.minimarket.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MiniMarket Plus API")
                        .version("1.0")
                        .description("""
                                API REST del monolito residual (catálogo, inventario, ventas).

                                **Autenticación:** obtener un JWT desde `auth-service` (`POST /api/auth/login` en puerto 8081) \
                                y usar el botón **Authorize** con el formato `Bearer {token}`.

                                **Roles:**
                                - **CLIENTE**: carrito propio y checkout autogestionado.
                                - **EMPLEADO**: operaciones de venta, inventario (lectura) y carrito de terceros.
                                - **GERENTE**: gestión de catálogo, inventario y operaciones de staff.
                                - **ADMIN**: acceso total a operaciones de negocio en este servicio.

                                **HATEOAS (HAL):** las respuestas de Producto, Carrito, Inventario y Venta \
                                usan Hypertext Application Language.
                                - Recurso individual (`EntityModel`): campos del dominio + `_links`.
                                - Colección (`CollectionModel`): `_embedded.<nombreLista>` + `_links` de la colección.
                                - Los enlaces de mutación (crear/actualizar/eliminar) solo aparecen según el rol del JWT.
                                """))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido desde auth-service POST /api/auth/login")));
    }
}
