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
                        .title("MiniMarket Plus - Catálogo e Inventario")
                        .version("1.0")
                        .description("""
                                API de productos, categorías e inventario.

                                **Autenticación:** JWT emitido por `auth-service` (`POST /api/auth/login` en puerto 8081).
                                Usar **Authorize** con `Bearer {token}`.

                                GET de productos/categorías es público. Mutaciones e inventario requieren roles de staff.
                                """))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido desde auth-service")));
    }
}
