package com.fernandez_terrones.prestamoservice.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

/**
 * Unico punto autorizado para resolver al prestamista autenticado.
 * El email SIEMPRE proviene del SecurityContextHolder (poblado por el JWT),
 * nunca de parametros ni del cuerpo de la peticion.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static String emailUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("No existe un usuario autenticado en el contexto de seguridad");
        }

        return authentication.getName();
    }
}
