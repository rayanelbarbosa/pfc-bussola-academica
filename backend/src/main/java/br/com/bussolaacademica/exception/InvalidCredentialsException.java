package br.com.bussolaacademica.exception;

/** Login inválido. A mensagem é genérica de propósito: não revela se o e-mail existe. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("E-mail ou senha inválidos.");
    }
}
