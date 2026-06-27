package com.minimarket.controller;

import com.minimarket.dto.ConfirmarPagoResponse;
import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Ventas", description = "Gestión de ventas y confirmación de pagos")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Operation(
            summary = "Listar ventas",
            description = "Roles: EMPLEADO, GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ventas"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    public List<Venta> listarVentas() {
        return ventaService.findAll();
    }

    @Operation(
            summary = "Listar ventas pendientes de pago",
            description = "Roles: EMPLEADO, GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ventas con estado PENDIENTE_PAGO"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/pendientes")
    public List<Venta> listarVentasPendientes() {
        return ventaService.findPendientesDePago();
    }

    @Operation(
            summary = "Obtener venta por ID",
            description = "Roles: EMPLEADO, GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta encontrada"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerVentaPorId(@PathVariable Long id) {
        Venta venta = ventaService.findById(id);
        return (venta != null) ? ResponseEntity.ok(venta) : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Crear venta",
            description = "Roles: EMPLEADO, GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Venta creada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    public Venta guardarVenta(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            name = "Nueva venta",
                            value = "{\"metodoPago\":\"EFECTIVO\",\"estadoPago\":\"PENDIENTE_PAGO\"}")))
            @RequestBody Venta venta) {
        return ventaService.save(venta);
    }

    @Operation(
            summary = "Confirmar pago de venta",
            description = "Roles: EMPLEADO, GERENTE, ADMIN. Marca la venta como pagada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago confirmado",
                    content = @Content(schema = @Schema(implementation = ConfirmarPagoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada"),
            @ApiResponse(responseCode = "422", description = "Estado de venta inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping("/{id}/confirmar-pago")
    public ResponseEntity<ConfirmarPagoResponse> confirmarPago(@PathVariable Long id) {
        Venta venta = ventaService.confirmarPago(id);
        ConfirmarPagoResponse response = new ConfirmarPagoResponse(venta.getId(), venta.getEstadoPago());
        return ResponseEntity.ok(response);
    }
}
