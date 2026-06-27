package com.minimarket.controller;

import com.minimarket.dto.ConfirmarPagoResponse;
import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public List<Venta> listarVentas() {
        return ventaService.findAll();
    }

    @GetMapping("/pendientes")
    public List<Venta> listarVentasPendientes() {
        return ventaService.findPendientesDePago();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerVentaPorId(@PathVariable Long id) {
        Venta venta = ventaService.findById(id);
        return (venta != null) ? ResponseEntity.ok(venta) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public Venta guardarVenta(@RequestBody Venta venta) {
        return ventaService.save(venta);
    }

    @PostMapping("/{id}/confirmar-pago")
    public ResponseEntity<ConfirmarPagoResponse> confirmarPago(@PathVariable Long id) {
        Venta venta = ventaService.confirmarPago(id);
        ConfirmarPagoResponse response = new ConfirmarPagoResponse(venta.getId(), venta.getEstadoPago());
        return ResponseEntity.ok(response);
    }
}
