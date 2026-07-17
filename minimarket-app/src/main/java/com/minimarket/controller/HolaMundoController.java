package com.minimarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Público", description = "Endpoints de acceso libre")
@RestController
public class HolaMundoController {

    @Operation(
            summary = "Saludo de prueba",
            description = "Público. Endpoint de health-check básico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saludo devuelto correctamente")
    })
    @GetMapping("/public/hola")
    public String holaMundo() {
        return "¡Hola Mundo!";
    }
}
