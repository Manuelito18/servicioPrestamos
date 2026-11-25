package com.fernandez_terrones.prestamoservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandez_terrones.prestamoservice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Respuesta 401 personalizada en JSON cuando falta el token, esta alterado o vencido.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        ErrorResponse cuerpo = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                "No autorizado: debe enviar un token JWT valido en la cabecera Authorization: Bearer <token>",
                request.getRequestURI()
        );

        objectMapper.writeValue(response.getOutputStream(), cuerpo);
    }
}
