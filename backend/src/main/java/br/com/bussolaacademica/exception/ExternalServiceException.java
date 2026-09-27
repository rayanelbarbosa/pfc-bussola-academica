package br.com.bussolaacademica.exception;

/** Falha ou indisponibilidade de uma API externa (ex.: YouTube). */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
