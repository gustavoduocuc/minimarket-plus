package com.minimarket.config;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.InventarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CatalogoDataInitializer implements CommandLineRunner {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;

    public CatalogoDataInitializer(
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            InventarioService inventarioService) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.inventarioService = inventarioService;
    }

    @Override
    public void run(String... args) {
        if (categoriaRepository.count() > 0) {
            return;
        }

        Categoria bebidas = createCategoria("Bebidas");
        Categoria lacteos = createCategoria("Lacteos");
        Categoria abarrotes = createCategoria("Abarrotes");

        createProductoConStock("Leche entera 1L", 1200.0, 50, lacteos);
        createProductoConStock("Agua mineral 500ml", 800.0, 100, bebidas);
        createProductoConStock("Arroz 1kg", 1500.0, 30, abarrotes);
    }

    private Categoria createCategoria(String nombre) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    private void createProductoConStock(String nombre, Double precio, Integer stockInicial, Categoria categoria) {
        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setCategoria(categoria);
        Producto guardado = productoRepository.save(producto);
        inventarioService.registrarEntrada(guardado.getId(), stockInicial);
    }
}
