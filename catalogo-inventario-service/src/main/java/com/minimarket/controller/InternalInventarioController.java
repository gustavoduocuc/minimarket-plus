package com.minimarket.controller;

import com.minimarket.dto.MovimientoStockRequest;
import com.minimarket.service.InventarioService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/internal/inventario")
public class InternalInventarioController {

    private final InventarioService inventarioService;
    private final String internalToken;

    public InternalInventarioController(
            InventarioService inventarioService,
            @Value("${catalogo.internal-token:MiniMarketInternalTokenLocal}") String internalToken) {
        this.inventarioService = inventarioService;
        this.internalToken = internalToken;
    }

    @PostMapping("/salidas")
    public ResponseEntity<Void> registrarSalida(
            @RequestHeader(value = "X-Internal-Token", required = false) String requestToken,
            @RequestBody MovimientoStockRequest request) {
        if (requestToken == null || !internalToken.equals(requestToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        inventarioService.registrarSalida(request.getProductoId(), request.getCantidad());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
