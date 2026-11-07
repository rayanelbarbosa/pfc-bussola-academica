package br.com.bussolaacademica.dto;

import jakarta.validation.constraints.NotNull;

/** Ativação ou desativação de um registro. */
public record StatusRequest(@NotNull(message = "informe o status") Boolean active) {
}
