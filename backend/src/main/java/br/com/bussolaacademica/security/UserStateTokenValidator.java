package br.com.bussolaacademica.security;

import br.com.bussolaacademica.repository.UserRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Confere o token contra o estado atual do usuário no banco: se a conta foi excluída ou se o perfil mudou
 * (ex.: um administrador foi rebaixado a estudante), o token antigo deixa de valer na hora, sem esperar expirar.
 */
public class UserStateTokenValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_SESSION =
            new OAuth2Error("invalid_token", "Sessão inválida. Entre novamente.", null);

    private final UserRepository userRepository;

    public UserStateTokenValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Long userId;
        try {
            userId = Long.valueOf(token.getSubject());
        } catch (NumberFormatException ex) {
            return OAuth2TokenValidatorResult.failure(INVALID_SESSION);
        }
        String tokenRole = token.getClaimAsString(TokenService.ROLE_CLAIM);
        boolean valid = userRepository.findById(userId)
                .map(user -> user.getRole().name().equals(tokenRole))
                .orElse(false);
        return valid ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(INVALID_SESSION);
    }
}
