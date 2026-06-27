package com.minimarket.controller;

import com.minimarket.security.model.AuthResponse;
import com.minimarket.security.model.LoginRequest;
import com.minimarket.security.model.RegistroRequest;
import com.minimarket.security.service.JwtTokenService;
import com.minimarket.security.service.LoginAttemptService;
import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Autenticación", description = "Login y registro de usuarios (endpoints públicos)")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenService jwtTokenService;
    private final UsuarioService usuarioService;
    private final LoginAttemptService loginAttemptService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtTokenService jwtTokenService,
            UsuarioService usuarioService,
            LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenService = jwtTokenService;
        this.usuarioService = usuarioService;
        this.loginAttemptService = loginAttemptService;
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Público. Autentica al usuario y devuelve un JWT para usar en el header Authorization.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas"),
            @ApiResponse(responseCode = "429", description = "Demasiados intentos de login")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> createAuthenticationToken(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            name = "Login cliente",
                            value = "{\"username\":\"cliente\",\"password\":\"Cliente123!\"}")))
            @Valid @RequestBody LoginRequest loginRequest) {
        loginAttemptService.assertNotLocked(loginRequest.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException badCredentialsException) {
            loginAttemptService.recordFailure(loginRequest.getUsername());
            throw badCredentialsException;
        }

        loginAttemptService.recordSuccess(loginRequest.getUsername());

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        String jwt = jwtTokenService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @Operation(
            summary = "Registrar usuario",
            description = "Público. Crea un nuevo usuario con rol CLIENTE por defecto.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o username duplicado")
    })
    @PostMapping("/registro")
    public ResponseEntity<Map<String, String>> registrarUsuario(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(examples = @ExampleObject(
                            name = "Registro",
                            value = "{\"username\":\"nuevoCliente\",\"password\":\"Cliente123!\"}")))
            @Valid @RequestBody RegistroRequest registroRequest) {
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(registroRequest.getUsername());
        nuevoUsuario.setPassword(registroRequest.getPassword());

        usuarioService.save(nuevoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Usuario registrado con éxito"));
    }
}
