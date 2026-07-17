package com.minimarket.service.impl;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public Usuario ensure(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username es obligatorio");
        }
        String usernameNormalizado = username.trim();
        return usuarioRepository.findByUsername(usernameNormalizado)
                .orElseGet(() -> crearProyeccion(usernameNormalizado));
    }

    private Usuario crearProyeccion(String username) {
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        return usuarioRepository.save(usuario);
    }
}
