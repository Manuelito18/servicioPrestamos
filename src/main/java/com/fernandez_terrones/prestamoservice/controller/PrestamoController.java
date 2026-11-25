package com.fernandez_terrones.prestamoservice.controller;

import com.fernandez_terrones.prestamoservice.dto.PrestamoRequest;
import com.fernandez_terrones.prestamoservice.dto.PrestamoResponse;
import com.fernandez_terrones.prestamoservice.dto.ResumenResponse;
import com.fernandez_terrones.prestamoservice.service.PrestamoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(PrestamoService prestamoService) {
        this.prestamoService = prestamoService;
    }

    @GetMapping
    public List<PrestamoResponse> listar() {
        return prestamoService.listar();
    }

    @GetMapping("/resumen")
    public ResumenResponse resumen() {
        return prestamoService.resumen();
    }

    @GetMapping("/{id}")
    public PrestamoResponse obtener(@PathVariable Long id) {
        return prestamoService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<PrestamoResponse> crear(@Valid @RequestBody PrestamoRequest request) {
        PrestamoResponse creado = prestamoService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public PrestamoResponse actualizar(@PathVariable Long id,
                                       @Valid @RequestBody PrestamoRequest request) {
        return prestamoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        prestamoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
