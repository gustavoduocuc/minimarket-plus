package com.minimarket.service.impl;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.CarritoService;
import com.minimarket.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;

    public CarritoServiceImpl(
            CarritoRepository carritoRepository,
            ProductoRepository productoRepository,
            UsuarioService usuarioService) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public Carrito agregarProducto(String username, Long productoId, int cantidad) {
        Usuario usuario = usuarioService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en BD"));

        Carrito carrito = carritoRepository.findByUsuarioId(usuario.getId())
                .orElseGet(() -> new Carrito(usuario));

        carrito.agregarProducto(producto, cantidad);
        return carritoRepository.save(carrito);
    }

    @Override
    public Optional<Carrito> obtenerCarritoDe(String username) {
        return usuarioService.findByUsername(username)
                .flatMap(usuario -> carritoRepository.findByUsuarioId(usuario.getId()));
    }

    @Override
    public List<Carrito> findAll() {
        return carritoRepository.findAll();
    }

    @Override
    public Optional<Carrito> findById(Long id) {
        return carritoRepository.findById(id);
    }

    @Override
    @Transactional
    public void quitarProducto(String username, Long productoId) {
        Carrito carrito = obtenerCarritoDe(username)
                .orElseThrow(() -> new IllegalStateException("No hay productos en el carrito"));
        carrito.quitarProducto(productoId);
        carritoRepository.save(carrito);
    }

    @Override
    @Transactional
    public void vaciarCarrito(String username) {
        obtenerCarritoDe(username).ifPresent(carrito -> {
            carrito.vaciar();
            carritoRepository.save(carrito);
        });
    }
}
