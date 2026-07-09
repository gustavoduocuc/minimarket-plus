package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.hateoas.InventarioModelAssembler;
import com.minimarket.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
@Tag(name = "Inventario", description = "Movimientos de inventario")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private InventarioModelAssembler inventarioModelAssembler;

    @Operation(
            summary = "Listar movimientos de inventario",
            description = "Roles: EMPLEADO, GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de movimientos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    public CollectionModel<EntityModel<Inventario>> listarMovimientosDeInventario() {
        return inventarioModelAssembler.toCollectionModel(inventarioService.findAll());
    }

    @Operation(
            summary = "Obtener movimiento por ID",
            description = "Roles: EMPLEADO, GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento encontrado"),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Inventario>> obtenerMovimientoPorId(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        return (inventario != null)
                ? ResponseEntity.ok(inventarioModelAssembler.toModel(inventario))
                : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Registrar movimiento de inventario",
            description = "Roles: GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento registrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping
    public EntityModel<Inventario> registrarMovimiento(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            name = "Entrada de stock",
                            value = """
                                    {"producto":{"id":1},"cantidad":10,"tipoMovimiento":"Entrada",\
                                    "fechaMovimiento":"2026-06-27T12:00:00.000+00:00"}\
                                    """)))
            @RequestBody Inventario inventario) {
        return inventarioModelAssembler.toModel(Objects.requireNonNull(inventarioService.save(inventario)));
    }

    @Operation(
            summary = "Actualizar movimiento de inventario",
            description = "Roles: GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento actualizado"),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Inventario>> actualizarMovimiento(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = Inventario.class)))
            @RequestBody Inventario inventario) {
        Inventario existente = inventarioService.findById(id);
        if (existente != null) {
            inventario.setId(id);
            Inventario inventarioActualizado = Objects.requireNonNull(inventarioService.save(inventario));
            return ResponseEntity.ok(inventarioModelAssembler.toModel(inventarioActualizado));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Eliminar movimiento de inventario",
            description = "Roles: GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Movimiento eliminado"),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
