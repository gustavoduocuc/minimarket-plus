package com.minimarket.dto;

import com.minimarket.security.validation.StrongPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class UsuarioRequestDto {

    @Schema(description = "Nombre de usuario", example = "empleado1")
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 20, message = "El nombre de usuario debe tener entre 3 y 20 caracteres")
    private String username;

    @Schema(description = "Contraseña del usuario", example = "Empleado123!")
    @StrongPassword
    @Size(max = 100, message = "La contraseña no puede superar 100 caracteres")
    private String password;

    @Schema(description = "Roles asignados al usuario", example = "[\"EMPLEADO\"]")
    private Set<String> roles;

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

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
