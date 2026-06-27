package com.minimarket.security.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @Schema(description = "Nombre de usuario", example = "cliente")
    @NotBlank(message = "El nombre de usuario es obligatorio y no puede estar vacío")
    private String username;

    @Schema(description = "Contraseña", example = "Cliente123!")
    @NotBlank(message = "La contraseña es obligatoria y no puede estar vacía")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
