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
                                API REST para la gestión de un minimarket.

                                **Autenticación:** obtener un JWT con `POST /api/auth/login` y usar el botón \
                                **Authorize** con el formato `Bearer {token}`.

                                **Roles:**
                                - **CLIENTE**: carrito propio y checkout autogestionado.
                                - **EMPLEADO**: operaciones de venta, inventario (lectura) y carrito de terceros.
                                - **GERENTE**: gestión de catálogo, inventario y operaciones de staff.
                                - **ADMIN**: acceso total, incluyendo gestión de usuarios.
                                """))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido desde POST /api/auth/login")));
    }
}
