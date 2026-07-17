package com.minimarket.service.impl;

import com.minimarket.catalogo.Producto;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.ForbiddenOperationException;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.CarritoService;
import com.minimarket.service.InventarioService;
import com.minimarket.service.ProductoService;
import com.minimarket.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class CarritoServiceImpl implements CarritoService {

    private static final Set<String> rolesStaff = Set.of("ROLE_ADMIN", "ROLE_GERENTE", "ROLE_EMPLEADO");

    private final CarritoRepository carritoRepository;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final InventarioService inventarioService;

    public CarritoServiceImpl(
            CarritoRepository carritoRepository,
            ProductoService productoService,
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository,
            InventarioService inventarioService) {
        this.carritoRepository = carritoRepository;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.inventarioService = inventarioService;
    }

    @Override
    @Transactional
    public Carrito agregarProducto(String callerUsername, Long usuarioObjetivoId, Long productoId, int cantidad) {
        Usuario caller = usuarioService.ensure(callerUsername);

        Long objetivoId = usuarioObjetivoId != null ? usuarioObjetivoId : caller.getId();
        validarPropiedadDelCarrito(objetivoId, caller.getId());

        Usuario usuarioObjetivo = resolverUsuarioObjetivo(caller, objetivoId);
        Long productoIdValido = requireNonNull(productoId, "Producto inválido");
        Producto producto = productoService.findById(productoIdValido);
        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado en catálogo");
        }

        int stockDisponible = inventarioService.consultarStockDisponible(productoIdValido);

        Carrito carrito = carritoRepository.findByUsuarioId(objetivoId)
                .orElseGet(() -> new Carrito(usuarioObjetivo));

        carrito.agregarProducto(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                cantidad,
                stockDisponible);
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

    private void validarPropiedadDelCarrito(Long objetivoId, Long callerId) {
        if (!puedeGestionarCarritosAjenos() && !objetivoId.equals(callerId)) {
            throw new ForbiddenOperationException("Un cliente solo puede modificar su propio carrito");
        }
    }

    private boolean puedeGestionarCarritosAjenos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(rolesStaff::contains);
    }

    private Usuario resolverUsuarioObjetivo(Usuario caller, Long objetivoId) {
        if (objetivoId.equals(caller.getId())) {
            return caller;
        }
        return usuarioRepository.findById(objetivoId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario objetivo no encontrado"));
    }
}
