package br.com.bussolaacademica.service;

import br.com.bussolaacademica.exception.BusinessRuleException;
import br.com.bussolaacademica.exception.TooManyLoginAttemptsException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Regra de negócio: ações que não podem ser desfeitas (excluir a própria conta, excluir a conta de outra pessoa e
 * alterar o perfil de acesso de alguém) pedem a senha de quem está logado de novo. Assim, um token roubado ou um
 * computador deixado aberto não bastam para fazer essas ações.
 *
 * <p>Usa o mesmo controle do login contra força bruta: cada senha errada conta como tentativa de login sem sucesso e,
 * depois de {@value AuthService#MAX_FAILED_ATTEMPTS} em 15 minutos, a conta fica bloqueada (login e confirmação).</p>
 */
@Service
public class PasswordConfirmationService {

    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public PasswordConfirmationService(PasswordEncoder passwordEncoder, AuditService auditService) {
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    /** Confere a senha do usuário; senha errada lança exceção e fica registrada na auditoria. */
    public void confirm(User user, String password) {
        if (auditService.countLoginFailuresSinceLastSuccess(user.getEmail(), AuthService.BLOCK_WINDOW)
                >= AuthService.MAX_FAILED_ATTEMPTS) {
            auditService.record(AuditAction.LOGIN_BLOCKED, user.getId(), user.getEmail(),
                    "Confirmação de senha bloqueada após " + AuthService.MAX_FAILED_ATTEMPTS + " tentativas sem sucesso",
                    false);
            throw new TooManyLoginAttemptsException(AuthService.BLOCK_WINDOW.toMinutes());
        }
        if (password == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            auditService.record(AuditAction.LOGIN_FAILURE, user.getId(), user.getEmail(),
                    "Senha incorreta ao confirmar uma ação crítica", false);
            throw new BusinessRuleException("Senha incorreta.");
        }
    }
}
