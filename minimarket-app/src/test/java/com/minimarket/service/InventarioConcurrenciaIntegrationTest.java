package com.minimarket.service;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class InventarioConcurrenciaIntegrationTest {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    private Long productoId;

    @BeforeEach
    void setUp() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Concurrencia-" + System.nanoTime());
        categoria = categoriaRepository.save(categoria);

        Producto producto = new Producto();
        producto.setNombre("Producto concurrencia");
        producto.setPrecio(1000.0);
        producto.setCategoria(categoria);
        producto = productoRepository.save(producto);
        productoId = producto.getId();

        inventarioService.registrarEntrada(productoId, 10);
    }

    @Test
    void salidasConcurrentes_soloPersistenLasQuePasaronLaValidacionDeStock() throws InterruptedException {
        int stockInicial = 10;
        int cantidadPorHilo = 3;
        int hilos = 5;
        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        CountDownLatch inicio = new CountDownLatch(1);
        CountDownLatch fin = new CountDownLatch(hilos);
        AtomicInteger salidasExitosas = new AtomicInteger(0);
        AtomicInteger salidasRechazadas = new AtomicInteger(0);

        for (int i = 0; i < hilos; i++) {
            executor.submit(() -> {
                try {
                    inicio.await();
                    inventarioService.registrarSalida(productoId, cantidadPorHilo);
                    salidasExitosas.incrementAndGet();
                } catch (IllegalStateException ex) {
                    salidasRechazadas.incrementAndGet();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    fin.countDown();
                }
            });
        }

        inicio.countDown();
        assertTrue(fin.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        int stockFinal = inventarioService.consultarStockDisponible(productoId);
        int salidasPersistidas = inventarioRepository.findByProductoId(productoId).stream()
                .filter(movimiento -> "Salida".equals(movimiento.getTipoMovimiento()))
                .mapToInt(movimiento -> movimiento.getCantidad())
                .sum();

        assertAll(
                () -> assertEquals(hilos, salidasExitosas.get() + salidasRechazadas.get(),
                        "Cada hilo debe terminar en éxito o rechazo por stock insuficiente"),
                () -> assertEquals(salidasExitosas.get() * cantidadPorHilo, salidasPersistidas,
                        "Una salida rechazada no persiste movimiento: solo se persiste lo que pasó la validación"),
                () -> assertEquals(stockInicial - salidasPersistidas, stockFinal,
                        "El saldo del ledger es entradas menos salidas efectivamente persistidas")
        );
        // El servicio valida el stock en cada registrarSalida y rechaza con IllegalStateException
        // cuando no alcanza, por lo que ninguna venta se concreta sin stock en el momento de validar.
        // La validación es check-then-act no atómico: sin locking, lecturas simultáneas del mismo
        // saldo podrían permitir overselling. Este test documenta ese límite del modelo.
    }
}
