package br.com.bussolaacademica.testevocacional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SubmeterTesteRequest(
        @NotEmpty @Valid List<RespostaDTO> respostas
) {
}
