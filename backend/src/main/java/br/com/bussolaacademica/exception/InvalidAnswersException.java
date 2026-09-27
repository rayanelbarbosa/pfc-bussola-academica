package br.com.bussolaacademica.exception;

/**
 * Lançada quando o conjunto de respostas não corresponde ao questionário:
 * pergunta inexistente, pergunta repetida ou pergunta sem resposta.
 */
public class InvalidAnswersException extends RuntimeException {

    public InvalidAnswersException(String message) {
        super(message);
    }
}
