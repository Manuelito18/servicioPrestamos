package com.fernandez_terrones.prestamoservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * POST /prestamos y PUT /prestamos/{id}.
 * fechaPrestamo NO se recibe: la asigna la API con LocalDate.now().
 */
public record PrestamoRequest(

        @NotBlank(message = "El cliente es obligatorio")
        @Size(max = 100, message = "El cliente admite maximo 100 caracteres")
        String cliente,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 digitos")
        String dni,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        @Digits(integer = 8, fraction = 2, message = "El monto admite hasta 8 enteros y 2 decimales")
        BigDecimal monto,

        @NotNull(message = "El interes es obligatorio")
        @DecimalMin(value = "0.00", message = "El interes no puede ser negativo")
        @Digits(integer = 3, fraction = 2, message = "El interes admite hasta 3 enteros y 2 decimales")
        BigDecimal interes,

        @NotNull(message = "La fecha de vencimiento es obligatoria")
        LocalDate fechaVencimiento,

        /** Opcional en el request; por defecto false al crear. */
        Boolean pagado
) {
}
