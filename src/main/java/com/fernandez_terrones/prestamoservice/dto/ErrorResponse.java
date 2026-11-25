package com.fernandez_terrones.prestamoservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

/**
 * Cuerpo JSON uniforme para errores HTTP (401, 403, 404, 409, 400, 500).
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        @JsonInclude(JsonInclude.Include.NON_NULL) Map<String, String> errores
) {

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                null
        );
    }

    public static ErrorResponse of(HttpStatus status, String message, String path, Map<String, String> errores) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                errores
        );
    }
}
