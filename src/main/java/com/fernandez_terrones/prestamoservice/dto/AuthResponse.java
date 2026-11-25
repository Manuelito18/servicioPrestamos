package com.fernandez_terrones.prestamoservice.dto;

/**
 * Respuesta de /auth/register y /auth/login: {"token": "..."}
 */
public record AuthResponse(String token) {
}
