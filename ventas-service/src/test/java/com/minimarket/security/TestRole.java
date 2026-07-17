package com.minimarket.security;

enum TestRole {
    CLIENTE("cliente", "Cliente123!"),
    EMPLEADO("empleado", "Empleado123!"),
    GERENTE("gerente", "Gerente123!"),
    ADMIN("admin", "Admin123!");

    private final String username;
    private final String password;

    TestRole(String username, String password) {
        this.username = username;
        this.password = password;
    }

    String username() {
        return username;
    }

    String password() {
        return password;
    }
}
