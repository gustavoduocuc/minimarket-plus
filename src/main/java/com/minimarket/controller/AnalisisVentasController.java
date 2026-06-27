package com.minimarket.controller;

import com.minimarket.dto.ProductoMasVendidoResponse;
import com.minimarket.service.AnalisisVentasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Análisis de Ventas", description = "Reportes y rankings de ventas")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/ventas/analisis")
public class AnalisisVentasController {

    @Autowired
    private AnalisisVentasService analisisVentasService;

    @Operation(
            summary = "Productos más vendidos",
            description = "Ranking de productos por cantidad total vendida. Solo considera ventas en estado PAGADO. Roles: EMPLEADO, GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking de productos más vendidos",
                    content = @Content(schema = @Schema(implementation = ProductoMasVendidoResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/productos-mas-vendidos")
    public List<ProductoMasVendidoResponse> productosMasVendidos() {
        return analisisVentasService.productosMasVendidos();
    }
}
