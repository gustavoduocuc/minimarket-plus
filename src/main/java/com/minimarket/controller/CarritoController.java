package com.minimarket.controller;

import com.minimarket.dto.AgregarItemCarritoRequest;
import com.minimarket.dto.CheckoutRequest;
import com.minimarket.dto.CheckoutResponse;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Venta;
import com.minimarket.service.CarritoCheckoutService;
import com.minimarket.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Carrito", description = "Gestión del carrito de compras y checkout")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;
    private final CarritoCheckoutService carritoCheckoutService;

    public CarritoController(CarritoService carritoService, CarritoCheckoutService carritoCheckoutService) {
        this.carritoService = carritoService;
        this.carritoCheckoutService = carritoCheckoutService;
    }

    @Operation(
            summary = "Obtener carrito propio",
            description = "Roles: CLIENTE, ADMIN. Devuelve el carrito del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito encontrado"),
            @ApiResponse(responseCode = "404", description = "Carrito no existe"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping
    public ResponseEntity<Carrito> obtenerCarritoPropio() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return carritoService.obtenerCarritoDe(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Listar todos los carritos",
            description = "Roles: EMPLEADO, GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de carritos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/todos")
    public List<Carrito> listarTodosLosCarritos() {
        return carritoService.findAll();
    }

    @Operation(
            summary = "Obtener carrito por ID",
            description = "Roles: EMPLEADO, GERENTE, ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito encontrado"),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Carrito> obtenerCarritoPorId(@PathVariable Long id) {
        return carritoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Checkout de carrito de un tercero",
            description = """
                    Roles: EMPLEADO, GERENTE, ADMIN.
                    Concreta la venta del carrito del usuario indicado (venta presencial).""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta creada",
                    content = @Content(schema = @Schema(implementation = CheckoutResponse.class))),
            @ApiResponse(responseCode = "400", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "422", description = "Carrito vacío o sin stock"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @PostMapping("/checkout/{usuarioId}")
    public ResponseEntity<CheckoutResponse> checkoutParaUsuario(
            @PathVariable Long usuarioId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            name = "Checkout presencial",
                            value = "{\"metodoPago\":\"EFECTIVO\"}")))
            @RequestBody CheckoutRequest request) {
        Venta venta = carritoCheckoutService.checkoutParaUsuario(usuarioId, request.getMetodoPago());
        return ResponseEntity.status(HttpStatus.CREATED).body(buildCheckoutResponse(venta));
    }

    @Operation(
            summary = "Checkout del carrito propio",
            description = "Autenticado. Concreta la venta del carrito del usuario del token.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Venta creada",
                    content = @Content(schema = @Schema(implementation = CheckoutResponse.class))),
            @ApiResponse(responseCode = "422", description = "Carrito vacío o sin stock"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            name = "Checkout autogestionado",
                            value = "{\"metodoPago\":\"DEBITO\"}")))
            @RequestBody CheckoutRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Venta venta = carritoCheckoutService.checkout(username, request.getMetodoPago());
        return ResponseEntity.status(HttpStatus.CREATED).body(buildCheckoutResponse(venta));
    }

    @Operation(
            summary = "Agregar producto al carrito",
            description = """
                    Roles: CLIENTE, EMPLEADO, GERENTE, ADMIN.
                    CLIENTE solo puede operar su propio carrito (403 si indica otro usuario).
                    STAFF puede indicar `usuario.id` para operar carritos ajenos.
                    Si se omite `usuario`, se usa el del token.""")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito actualizado"),
            @ApiResponse(responseCode = "403", description = "CLIENTE intentando carrito ajeno"),
            @ApiResponse(responseCode = "422", description = "Stock insuficiente"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @PostMapping
    public Carrito agregarProductoAlCarrito(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = {
                            @ExampleObject(
                                    name = "Carrito propio",
                                    value = "{\"producto\":{\"id\":1},\"cantidad\":2}"),
                            @ExampleObject(
                                    name = "Carrito de tercero (staff)",
                                    value = "{\"usuario\":{\"id\":4},\"producto\":{\"id\":1},\"cantidad\":2}")
                    }))
            @RequestBody AgregarItemCarritoRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long usuarioObjetivoId = request.getUsuario() != null ? request.getUsuario().getId() : null;
        return carritoService.agregarProducto(
                username,
                usuarioObjetivoId,
                request.getProducto().getId(),
                request.getCantidad());
    }

    @Operation(
            summary = "Quitar producto del carrito",
            description = "Roles: CLIENTE, ADMIN. Opera sobre el carrito del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado del carrito"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<Void> quitarProductoDelCarrito(@PathVariable Long productoId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        carritoService.quitarProducto(username, productoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Vaciar carrito",
            description = "Roles: CLIENTE, ADMIN. Vacía el carrito del usuario autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Carrito vaciado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos")
    })
    @DeleteMapping
    public ResponseEntity<Void> vaciarCarrito() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        carritoService.vaciarCarrito(username);
        return ResponseEntity.noContent().build();
    }

    private CheckoutResponse buildCheckoutResponse(Venta venta) {
        return new CheckoutResponse(
                venta.getId(),
                venta.getMetodoPago(),
                venta.getEstadoPago(),
                venta.calculateTotal());
    }
}
