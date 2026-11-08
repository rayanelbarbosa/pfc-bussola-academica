package br.com.bussolaacademica.security;

import br.com.bussolaacademica.repository.RevokedTokenRepository;
import br.com.bussolaacademica.repository.UserRepository;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Confere o token contra o estado atual no banco, para ele deixar de valer na hora, sem esperar expirar:
 * <ul>
 *   <li>token revogado no logout;</li>
 *   <li>conta excluída;</li>
 *   <li>perfil alterado (ex.: um administrador foi rebaixado a estudante).</li>
 * </ul>
 * Tokens sem identificador (jti), gerados antes do logout real existir, também são recusados.
 */
public class UserStateTokenValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error INVALID_SESSION =
            new OAuth2Error("invalid_token", "Sessão inválida. Entre novamente.", null);

    private final UserRepository userRepository;
    private final RevokedTokenRepository revokedTokenRepository;

    public UserStateTokenValidator(UserRepository userRepository, RevokedTokenRepository revokedTokenRepository) {
        this.userRepository = userRepository;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        Long userId;
        try {
            userId = Long.valueOf(token.getSubject());
        } catch (NumberFormatException ex) {
            return OAuth2TokenValidatorResult.failure(INVALID_SESSION);
        }
        if (token.getId() == null || revokedTokenRepository.existsById(token.getId())) {
            return OAuth2TokenValidatorResult.failure(INVALID_SESSION);
        }
        String tokenRole = token.getClaimAsString(TokenService.ROLE_CLAIM);
        boolean valid = userRepository.findById(userId)
                .map(user -> user.getRole().name().equals(tokenRole))
                .orElse(false);
        return valid ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(INVALID_SESSION);
    }
}
