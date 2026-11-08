package br.com.bussolaacademica.exception;

/** Login bloqueado temporariamente depois de muitas senhas erradas seguidas (proteção contra força bruta). */
public class TooManyLoginAttemptsException extends RuntimeException {

    public TooManyLoginAttemptsException(long minutes) {
        super("Muitas tentativas de login sem sucesso. Aguarde " + minutes + " minutos e tente novamente.");
    }
}
