package com.fernandez_terrones.prestamoservice.security;

import com.fernandez_terrones.prestamoservice.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Generacion y validacion de tokens JWT firmados con HS256.
 * El subject del token es el email del prestamista.
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiration}") long expiracionMs) {
        byte[] bytes = secreto == null ? new byte[0] : secreto.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 caracteres (256 bits) para firmar con HS256");
        }
        if (expiracionMs <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION debe ser un numero de milisegundos mayor a 0");
        }
        this.clave = Keys.hmacShaKeyFor(bytes);
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusMillis(expiracionMs)))
                .signWith(clave)
                .compact();
    }

    public String extraerEmail(String token) {
        return parsear(token).getSubject();
    }

    public boolean esTokenValido(String token, UserDetails userDetails) {
        try {
            Claims claims = parsear(token);
            return claims.getSubject() != null
                    && claims.getSubject().equals(userDetails.getUsername())
                    && claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long getExpiracionMs() {
        return expiracionMs;
    }

    private Claims parsear(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
