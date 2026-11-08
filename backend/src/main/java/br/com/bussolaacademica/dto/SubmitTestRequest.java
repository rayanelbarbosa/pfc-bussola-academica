package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.InterestArea;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Respostas do questionário e as áreas de interesse escolhidas (de 1 a 3; vazio = "Ainda não sei"). */
public record SubmitTestRequest(
        @NotEmpty(message = "envie as respostas do questionário") @Valid List<AnswerRequest> answers,
        @Size(max = 3, message = "escolha no máximo 3 áreas de interesse") List<InterestArea> interestAreas
) {

    public SubmitTestRequest(List<AnswerRequest> answers) {
        this(answers, null);
    }
}
