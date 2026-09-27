package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.Question;

/** Pergunta como é exposta pela API. */
public record QuestionResponse(Integer id, String statement, String category) {

    public static QuestionResponse from(Question question) {
        return new QuestionResponse(question.getId(), question.getStatement(), question.getCategory().name());
    }
}
