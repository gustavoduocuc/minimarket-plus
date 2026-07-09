package com.minimarket.controller;

import com.minimarket.dto.StockDisponibleResponse;
import com.minimarket.entity.Producto;
import com.minimarket.hateoas.ProductoModelAssembler;
import com.minimarket.service.ProductoService;
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
@Tag(name = "Productos", description = "Catálogo de productos")
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoModelAssembler productoModelAssembler;

    @Operation(
            summary = "Listar productos",
            description = "Público. Incluye stockDisponible calculado desde inventario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de productos. Incluye enlaces HATEOAS en _links")
    })
    @GetMapping
    public CollectionModel<EntityModel<Producto>> listarProductos() {
        return productoModelAssembler.toCollectionModel(productoService.findAll());
    }

    @Operation(
            summary = "Consultar stock disponible de un producto",
            description = "Público. Devuelve el stock calculado desde movimientos de inventario (Entrada - Salida).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock disponible",
                    content = @Content(schema = @Schema(implementation = StockDisponibleResponse.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}/stock")
    public ResponseEntity<StockDisponibleResponse> consultarStockDisponible(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        int stockDisponible = productoService.consultarStock(id);
        return ResponseEntity.ok(new StockDisponibleResponse(id, stockDisponible));
    }

    @Operation(
            summary = "Obtener producto por ID",
            description = "Público. Incluye stockDisponible calculado desde inventario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado. Incluye enlaces HATEOAS en _links"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> obtenerProductoPorId(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        return (producto != null)
                ? ResponseEntity.ok(productoModelAssembler.toModel(producto))
                : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Crear producto",
            description = "Roles: GERENTE, ADMIN. El stock se gestiona vía inventario (POST /api/inventario).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto creado. Incluye enlaces HATEOAS en _links"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public EntityModel<Producto> guardarProducto(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            name = "Nuevo producto",
                            value = """
                                    {"nombre":"Leche entera 1L","precio":990.0,\
                                    "categoria":{"id":1}}\
                                    """)))
            @RequestBody Producto producto) {
        return productoModelAssembler.toModel(Objects.requireNonNull(productoService.save(producto)));
    }

    @Operation(
            summary = "Actualizar producto",
            description = "Roles: GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado. Incluye enlaces HATEOAS en _links"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> actualizarProducto(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = Producto.class)))
            @RequestBody Producto producto) {
        Producto productoExistente = productoService.findById(id);
        if (productoExistente != null) {
            producto.setId(id);
            Producto productoActualizado = Objects.requireNonNull(productoService.save(producto));
            return ResponseEntity.ok(productoModelAssembler.toModel(productoActualizado));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Eliminar producto",
            description = "Roles: GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
