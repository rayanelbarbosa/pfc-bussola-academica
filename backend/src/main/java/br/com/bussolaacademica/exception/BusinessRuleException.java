package br.com.bussolaacademica.exception;

/** Operação válida no formato, mas proibida por uma regra de negócio (responde 422). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
