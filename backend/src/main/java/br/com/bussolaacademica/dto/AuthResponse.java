package br.com.bussolaacademica.dto;

/** Resposta do login/cadastro: o JWT (enviado depois no cabeçalho Authorization) e os dados do usuário. */
public record AuthResponse(String token, String tokenType, long expiresInSeconds, UserResponse user) {
}
