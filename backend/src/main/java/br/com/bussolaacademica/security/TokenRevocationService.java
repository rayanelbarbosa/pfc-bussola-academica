package br.com.bussolaacademica.security;

import br.com.bussolaacademica.model.RevokedToken;
import br.com.bussolaacademica.repository.RevokedTokenRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Logout real: o JWT é stateless, então "sair" no front só apaga o token do navegador. Para o token
 * deixar de valer também no servidor, o identificador dele (jti) entra numa lista de bloqueio até expirar.
 */
@Service
public class TokenRevocationService {

    private final RevokedTokenRepository repository;
    private final Clock clock;

    public TokenRevocationService(RevokedTokenRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void revoke(Jwt token) {
        if (token.getId() == null || repository.existsById(token.getId())) {
            return;
        }
        Instant expiresAt = token.getExpiresAt() != null ? token.getExpiresAt() : clock.instant();
        repository.save(new RevokedToken(token.getId(), Long.valueOf(token.getSubject()),
                LocalDateTime.ofInstant(expiresAt, clock.getZone()), LocalDateTime.now(clock)));
    }

    /** Remove da lista os tokens que já expiraram (rodado pela rotina diária de manutenção). */
    @Transactional
    public int purgeExpired() {
        return repository.deleteExpired(LocalDateTime.now(clock));
    }
}
