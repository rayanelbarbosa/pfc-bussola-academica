package br.com.bussolaacademica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmitTestRequest(
        @NotEmpty(message = "envie as respostas do questionário") @Valid List<AnswerRequest> answers
) {
}
