package com.minimarket.service;

import com.minimarket.entity.Usuario;

import java.util.Optional;

public interface UsuarioService {
    Optional<Usuario> findByUsername(String username);

    Usuario ensure(String username);
}
