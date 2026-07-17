package com.minimarket.dto;

import jakarta.validation.constraints.NotBlank;

public class EnsureUsuarioRequest {

    @NotBlank
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
