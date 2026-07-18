package com.minimarket.hateoas;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class HateoasSecurityHelper {

    public boolean tieneRol(String rol) {
        return tieneAuthority("ROLE_" + rol);
    }

    public boolean tieneAlgunRol(String... roles) {
        for (String rol : roles) {
            if (tieneRol(rol)) {
                return true;
            }
        }
        return false;
    }

    public boolean puedeGestionarProductos() {
        return tieneAlgunRol("GERENTE", "ADMIN");
    }

    public boolean puedeConsultarInventario() {
        return tieneAlgunRol("EMPLEADO", "GERENTE", "ADMIN");
    }

    public boolean puedeGestionarInventario() {
        return tieneAlgunRol("GERENTE", "ADMIN");
    }

    private boolean tieneAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }
}
