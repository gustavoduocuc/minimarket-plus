package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public Usuario save(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setPassword(encodePassword(usuario.getPassword()));
            if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
                Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                        .orElseThrow(() -> new IllegalStateException("Rol CLIENTE no existe en la base de datos"));
                Set<Rol> rolesPorDefecto = new HashSet<>();
                rolesPorDefecto.add(rolCliente);
                usuario.setRoles(rolesPorDefecto);
            }
        } else {
            usuarioRepository.findById(usuario.getId()).ifPresent(existing -> {
                if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                    usuario.setPassword(existing.getPassword());
                } else if (!isEncoded(usuario.getPassword())) {
                    usuario.setPassword(encodePassword(usuario.getPassword()));
                }
            });
        }
        return usuarioRepository.save(usuario);
    }

    private String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (isEncoded(rawPassword)) {
            return rawPassword;
        }
        return passwordEncoder.encode(rawPassword);
    }

    private boolean isEncoded(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$");
    }
}
