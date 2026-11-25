package com.fernandez_terrones.prestamoservice.service;

import com.fernandez_terrones.prestamoservice.dto.AuthResponse;
import com.fernandez_terrones.prestamoservice.dto.LoginRequest;
import com.fernandez_terrones.prestamoservice.dto.RegisterRequest;
import com.fernandez_terrones.prestamoservice.entity.Usuario;
import com.fernandez_terrones.prestamoservice.exception.EmailAlreadyExistsException;
import com.fernandez_terrones.prestamoservice.exception.InvalidCredentialsException;
import com.fernandez_terrones.prestamoservice.repository.UsuarioRepository;
import com.fernandez_terrones.prestamoservice.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private static final String ROL_POR_DEFECTO = "USER";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registrar(RegisterRequest request) {
        String email = normalizarEmail(request.email());

        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("El email '" + email + "' ya esta registrado");
        }

        Usuario usuario = new Usuario(
                request.nombre().trim(),
                email,
                passwordEncoder.encode(request.password()),
                ROL_POR_DEFECTO
        );

        Usuario guardado = usuarioRepository.save(usuario);
        return new AuthResponse(jwtService.generarToken(guardado));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizarEmail(request.email());

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Email o password incorrectos"));

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new InvalidCredentialsException("Email o password incorrectos");
        }

        return new AuthResponse(jwtService.generarToken(usuario));
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
