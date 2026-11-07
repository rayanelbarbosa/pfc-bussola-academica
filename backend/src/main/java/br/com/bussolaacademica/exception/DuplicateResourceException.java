package br.com.bussolaacademica.exception;

/** Tentativa de cadastrar um registro que já existe (responde 409 Conflict). */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
