package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Detalhes de um usuário para o administrador: cadastro, aceite dos termos e histórico de testes (resumo de cada um).
 * Nunca inclui a senha, o hash nem as respostas do questionário.
 */
public record AdminUserDetailResponse(Long id, String name, String email, String role, LocalDateTime createdAt,
                                      String termsVersion, LocalDateTime termsAcceptedAt,
                                      List<ResultSummaryResponse> results) {

    public static AdminUserDetailResponse from(User user, List<ResultSummaryResponse> results) {
        return new AdminUserDetailResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name(),
                user.getCreatedAt(), user.getTermsVersion(), user.getTermsAcceptedAt(), results);
    }
}
