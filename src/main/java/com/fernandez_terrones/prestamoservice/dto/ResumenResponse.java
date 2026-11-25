package com.fernandez_terrones.prestamoservice.dto;

import java.math.BigDecimal;

/**
 * GET /prestamos/resumen
 */
public record ResumenResponse(
        BigDecimal capitalPrestado,
        BigDecimal totalPorCobrar,
        BigDecimal gananciaEsperada
) {
}
