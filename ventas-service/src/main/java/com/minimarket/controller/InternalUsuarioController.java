package com.minimarket.controller;

import com.minimarket.dto.EnsureUsuarioRequest;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
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
@RequestMapping("/internal/usuarios")
public class InternalUsuarioController {

    private final UsuarioService usuarioService;
    private final String internalToken;

    public InternalUsuarioController(
            UsuarioService usuarioService,
            @Value("${ventas.internal-token:MiniMarketInternalTokenLocal}") String internalToken) {
        this.usuarioService = usuarioService;
        this.internalToken = internalToken;
    }

    @PostMapping
    public ResponseEntity<Void> ensureUsuario(
            @RequestHeader(value = "X-Internal-Token", required = false) String requestToken,
            @Valid @RequestBody EnsureUsuarioRequest request) {
        if (requestToken == null || !internalToken.equals(requestToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        usuarioService.ensure(request.getUsername());
        return ResponseEntity.noContent().build();
    }
}
