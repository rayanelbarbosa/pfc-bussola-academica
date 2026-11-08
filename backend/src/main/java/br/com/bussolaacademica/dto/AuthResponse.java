package br.com.bussolaacademica.dto;

/**
 * Resposta do login/cadastro: o JWT (enviado depois no cabeçalho Authorization) e os dados do usuário.
 * {@code pendingTerms} vem preenchido quando existe uma versão nova do Termo/Política que o usuário
 * ainda não aceitou; enquanto isso a API só libera o novo aceite, os próprios dados e o logout.
 */
public record AuthResponse(String token, String tokenType, long expiresInSeconds, UserResponse user,
                           TermsVersionResponse pendingTerms) {
}
