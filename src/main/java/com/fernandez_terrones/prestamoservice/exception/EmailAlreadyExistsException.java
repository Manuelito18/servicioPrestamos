package com.fernandez_terrones.prestamoservice.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String mensaje) {
        super(mensaje);
    }
}
