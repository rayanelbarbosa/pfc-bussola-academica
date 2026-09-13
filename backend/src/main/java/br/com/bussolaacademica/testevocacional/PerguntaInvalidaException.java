package br.com.bussolaacademica.testevocacional;

public class PerguntaInvalidaException extends RuntimeException {

    public PerguntaInvalidaException(Integer perguntaId) {
        super("Pergunta inexistente: id=" + perguntaId);
    }
}
