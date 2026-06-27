package com.minimarket.controller;

import com.minimarket.dto.AgregarItemCarritoRequest;
import com.minimarket.dto.CheckoutRequest;
import com.minimarket.dto.CheckoutResponse;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Venta;
import com.minimarket.service.CarritoCheckoutService;
import com.minimarket.service.CarritoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;
    private final CarritoCheckoutService carritoCheckoutService;

    public CarritoController(CarritoService carritoService, CarritoCheckoutService carritoCheckoutService) {
        this.carritoService = carritoService;
        this.carritoCheckoutService = carritoCheckoutService;
    }

    @GetMapping
    public ResponseEntity<Carrito> obtenerCarritoPropio() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return carritoService.obtenerCarritoDe(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/todos")
    public List<Carrito> listarTodosLosCarritos() {
        return carritoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carrito> obtenerCarritoPorId(@PathVariable Long id) {
        return carritoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@RequestBody CheckoutRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Venta venta = carritoCheckoutService.checkout(username, request.getMetodoPago());
        CheckoutResponse response = new CheckoutResponse(
                venta.getId(),
                venta.getMetodoPago(),
                venta.getEstadoPago(),
                venta.calculateTotal());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    public Carrito agregarProductoAlCarrito(@RequestBody AgregarItemCarritoRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return carritoService.agregarProducto(
                username,
                request.getProducto().getId(),
                request.getCantidad());
    }

    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<Void> quitarProductoDelCarrito(@PathVariable Long productoId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        carritoService.quitarProducto(username, productoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> vaciarCarrito() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        carritoService.vaciarCarrito(username);
        return ResponseEntity.noContent().build();
    }
}
