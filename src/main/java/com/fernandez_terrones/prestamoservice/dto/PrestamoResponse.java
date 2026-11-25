package com.fernandez_terrones.prestamoservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Contrato de respuesta de un prestamo. totalCobrar lo calcula el backend.
 */
public record PrestamoResponse(
        Long id,
        String cliente,
        String dni,
        BigDecimal monto,
        BigDecimal interes,
        BigDecimal totalCobrar,
        LocalDate fechaPrestamo,
        LocalDate fechaVencimiento,
        boolean pagado
) {
}
