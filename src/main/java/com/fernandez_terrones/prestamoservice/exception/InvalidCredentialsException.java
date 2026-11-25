package com.fernandez_terrones.prestamoservice.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String mensaje) {
        super(mensaje);
    }
}
