package com.minimarket.service.impl;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.ForbiddenOperationException;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.CarritoService;
import com.minimarket.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class CarritoServiceImpl implements CarritoService {

    private static final Set<String> rolesStaff = Set.of("ADMIN", "GERENTE", "EMPLEADO");

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    public CarritoServiceImpl(
            CarritoRepository carritoRepository,
            ProductoRepository productoRepository,
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public Carrito agregarProducto(String callerUsername, Long usuarioObjetivoId, Long productoId, int cantidad) {
        Usuario caller = usuarioService.findByUsername(callerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Long objetivoId = usuarioObjetivoId != null ? usuarioObjetivoId : caller.getId();
        validarPropiedadDelCarrito(caller, objetivoId);

        Usuario usuarioObjetivo = resolverUsuarioObjetivo(caller, objetivoId);
        Long productoIdValido = requireNonNull(productoId, "Producto inválido");
        Producto producto = productoRepository.findById(productoIdValido)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en BD"));

        Carrito carrito = carritoRepository.findByUsuarioId(objetivoId)
                .orElseGet(() -> new Carrito(usuarioObjetivo));

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

    private void validarPropiedadDelCarrito(Usuario caller, Long objetivoId) {
        if (!puedeGestionarCarritosAjenos(caller) && !objetivoId.equals(caller.getId())) {
            throw new ForbiddenOperationException("Un cliente solo puede modificar su propio carrito");
        }
    }

    private boolean puedeGestionarCarritosAjenos(Usuario usuario) {
        return rolesStaff.stream().anyMatch(usuario::tieneRol);
    }

    private Usuario resolverUsuarioObjetivo(Usuario caller, Long objetivoId) {
        if (objetivoId.equals(caller.getId())) {
            return caller;
        }
        return usuarioRepository.findById(objetivoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario objetivo no encontrado"));
    }
}
