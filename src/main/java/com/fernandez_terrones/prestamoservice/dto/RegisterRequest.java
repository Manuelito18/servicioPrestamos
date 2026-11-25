package com.fernandez_terrones.prestamoservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * POST /auth/register
 */
public record RegisterRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite maximo 100 caracteres")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        @Size(max = 120, message = "El email admite maximo 120 caracteres")
        String email,

        @NotBlank(message = "La password es obligatoria")
        @Size(min = 8, max = 72, message = "La password debe tener entre 8 y 72 caracteres")
        String password
) {
}
