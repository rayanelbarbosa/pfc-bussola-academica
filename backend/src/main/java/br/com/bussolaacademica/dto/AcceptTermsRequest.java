package br.com.bussolaacademica.dto;

import jakarta.validation.constraints.NotBlank;

/** Novo aceite: o usuário informa qual versão leu e aceitou. */
public record AcceptTermsRequest(@NotBlank(message = "a versão é obrigatória") String version) {
}
