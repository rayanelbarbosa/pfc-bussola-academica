package br.com.bussolaacademica.service;

import br.com.bussolaacademica.config.MailProperties;
import br.com.bussolaacademica.dto.ForgotPasswordRequest;
import br.com.bussolaacademica.dto.ResetPasswordRequest;
import br.com.bussolaacademica.exception.BusinessRuleException;
import br.com.bussolaacademica.integration.email.EmailSender;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.PasswordResetToken;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.repository.PasswordResetTokenRepository;
import br.com.bussolaacademica.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Recuperação de senha por e-mail (RF04).
 *
 * <ul>
 *   <li>Mensagem neutra: a resposta é a mesma para e-mail cadastrado ou não (não revela quem tem conta).</li>
 *   <li>Token aleatório de 256 bits, de uso único, válido por 30 minutos; no banco fica só o hash SHA-256.</li>
 *   <li>Um pedido novo anula os links anteriores; no máximo um e-mail a cada 2 minutos por conta.</li>
 *   <li>Ao trocar a senha, todas as sessões abertas (tokens JWT emitidos antes) deixam de valer.</li>
 * </ul>
 */
@Service
public class PasswordResetService {

    static final Duration TOKEN_VALIDITY = Duration.ofMinutes(30);
    static final Duration RESEND_INTERVAL = Duration.ofMinutes(2);
    static final String INVALID_LINK = "Este link de redefinição é inválido ou expirou. Peça um novo em \"Esqueci minha senha\".";
    private static final String SUBJECT = "Redefinição de senha da Bússola Acadêmica";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final MailProperties mailProperties;
    private final AuditService auditService;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder, EmailSender emailSender,
                                MailProperties mailProperties, AuditService auditService, Clock clock) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.mailProperties = mailProperties;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public void requestReset(ForgotPasswordRequest request) {
        String email = AuthService.normalize(request.email());
        LocalDateTime now = LocalDateTime.now(clock);
        Optional<User> found = userRepository.findByEmailIgnoreCase(email);
        if (found.isEmpty()) {
            auditService.record(AuditAction.PASSWORD_RESET_REQUESTED, null, email,
                    "E-mail não cadastrado: nada foi enviado", false);
            return;
        }
        User user = found.get();
        if (tokenRepository.existsByUserIdAndCreatedAtAfter(user.getId(), now.minus(RESEND_INTERVAL))) {
            auditService.record(AuditAction.PASSWORD_RESET_REQUESTED, user.getId(), email,
                    "Pedido repetido em menos de " + RESEND_INTERVAL.toMinutes() + " min: nada foi enviado", false);
            return;
        }
        tokenRepository.invalidateOpenTokens(user.getId(), now);
        String token = newToken();
        tokenRepository.save(new PasswordResetToken(user.getId(), sha256(token), now, now.plus(TOKEN_VALIDITY)));
        String link = resetLink(token);
        emailSender.send(user.getEmail(), SUBJECT, html(user.getName(), link), text(user.getName(), link));
        auditService.record(AuditAction.PASSWORD_RESET_REQUESTED, user.getId(), email,
                "Link de redefinição enviado por e-mail (válido por " + TOKEN_VALIDITY.toMinutes() + " min)", true);
    }

    /** Confere o link assim que a tela abre, para não deixar a pessoa digitar a senha à toa. */
    @Transactional(readOnly = true)
    public void validateToken(String token) {
        LocalDateTime now = LocalDateTime.now(clock);
        boolean usable = tokenRepository.findByTokenHash(sha256(token.trim()))
                .filter(candidate -> candidate.isUsable(now))
                .isPresent();
        if (!usable) {
            throw new BusinessRuleException(INVALID_LINK);
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        LocalDateTime now = LocalDateTime.now(clock);
        Optional<PasswordResetToken> token = tokenRepository.findByTokenHash(sha256(request.token().trim()))
                .filter(candidate -> candidate.isUsable(now));
        Optional<User> user = token.flatMap(found -> userRepository.findById(found.getUserId()));
        if (user.isEmpty()) {
            auditService.record(AuditAction.PASSWORD_RESET_COMPLETED, null, null,
                    "Link de redefinição inválido, já usado ou expirado", false);
            throw new BusinessRuleException(INVALID_LINK);
        }
        user.get().changePassword(passwordEncoder.encode(request.newPassword()), now.truncatedTo(ChronoUnit.SECONDS));
        token.get().markUsed(now);
        tokenRepository.invalidateOpenTokens(user.get().getId(), now);
        auditService.record(AuditAction.PASSWORD_RESET_COMPLETED, user.get().getId(), user.get().getEmail(),
                "Senha redefinida pelo link enviado por e-mail; sessões abertas encerradas", true);
    }

    /** Remove os pedidos vencidos há mais de 1 dia (rodado pela rotina diária de limpeza). */
    @Transactional
    public int purgeExpired() {
        return tokenRepository.deleteExpiredBefore(LocalDateTime.now(clock).minusDays(1));
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível", ex);
        }
    }

    private String resetLink(String token) {
        String base = mailProperties.frontendUrl().replaceAll("/+$", "");
        return base + "/redefinir-senha?token=" + token;
    }

    private static String firstName(String name) {
        String trimmed = name == null ? "" : name.trim();
        int space = trimmed.indexOf(' ');
        return space > 0 ? trimmed.substring(0, space) : trimmed;
    }

    private static String html(String name, String link) {
        String safeName = HtmlUtils.htmlEscape(firstName(name));
        String safeLink = HtmlUtils.htmlEscape(link);
        return """
                <div style="font-family:Inter,Arial,sans-serif;max-width:480px;margin:0 auto;color:#111111">
                  <h1 style="font-size:20px;font-weight:600">Redefinição de senha</h1>
                  <p style="font-size:15px;line-height:1.5">Olá, %s.</p>
                  <p style="font-size:15px;line-height:1.5">Recebemos um pedido para criar uma nova senha na Bússola Acadêmica. Clique no botão abaixo para continuar. O link vale por 30 minutos e só pode ser usado uma vez.</p>
                  <p style="margin:24px 0"><a href="%s" style="background:#7C3AED;color:#ffffff;text-decoration:none;padding:12px 20px;border-radius:999px;font-weight:600;display:inline-block">Criar nova senha</a></p>
                  <p style="font-size:13px;line-height:1.5;color:#6B6B6B">Se o botão não funcionar, copie e cole este endereço no navegador:<br>%s</p>
                  <p style="font-size:13px;line-height:1.5;color:#6B6B6B">Se você não pediu a troca de senha, ignore este e-mail. Sua senha atual continua valendo.</p>
                </div>
                """.formatted(safeName, safeLink, safeLink);
    }

    private static String text(String name, String link) {
        return """
                Olá, %s.

                Recebemos um pedido para criar uma nova senha na Bússola Acadêmica.
                Acesse o link abaixo para continuar. Ele vale por 30 minutos e só pode ser usado uma vez.

                %s

                Se você não pediu a troca de senha, ignore este e-mail. Sua senha atual continua valendo.
                """.formatted(firstName(name), link);
    }
}
