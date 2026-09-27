package br.com.bussolaacademica.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException() {
        super("Já existe uma conta com este e-mail.");
    }
}
