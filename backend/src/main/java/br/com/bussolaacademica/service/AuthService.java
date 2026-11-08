package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.AcceptTermsRequest;
import br.com.bussolaacademica.dto.AuthResponse;
import br.com.bussolaacademica.dto.LoginRequest;
import br.com.bussolaacademica.dto.RegisterRequest;
import br.com.bussolaacademica.dto.UserResponse;
import br.com.bussolaacademica.exception.BusinessRuleException;
import br.com.bussolaacademica.exception.EmailAlreadyUsedException;
import br.com.bussolaacademica.exception.InvalidCredentialsException;
import br.com.bussolaacademica.exception.ResourceNotFoundException;
import br.com.bussolaacademica.exception.TooManyLoginAttemptsException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.security.CurrentUser;
import br.com.bussolaacademica.security.TokenRevocationService;
import br.com.bussolaacademica.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

/**
 * Cadastro, login, logout e novo aceite dos termos. Todo cadastro novo recebe o perfil STUDENT.
 *
 * <ul>
 *   <li>Força bruta: depois de 5 senhas erradas em 15 minutos para o mesmo e-mail, o login fica bloqueado
 *       até a janela passar (a contagem usa o próprio log de auditoria).</li>
 *   <li>Tempo constante: quando o e-mail não existe, a senha é comparada com um hash fictício, assim a
 *       resposta demora o mesmo que uma senha errada e não revela quais e-mails têm conta.</li>
 *   <li>Logout real: o token é revogado no servidor.</li>
 * </ul>
 */
@Service
public class AuthService {

    static final int MAX_FAILED_ATTEMPTS = 5;
    static final Duration BLOCK_WINDOW = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final TokenRevocationService tokenRevocationService;
    private final TermsService termsService;
    private final CurrentUser currentUser;
    private final AuditService auditService;
    private final Clock clock;
    private final String dummyPasswordHash;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService,
                       TokenRevocationService tokenRevocationService, TermsService termsService,
                       CurrentUser currentUser, AuditService auditService, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.tokenRevocationService = tokenRevocationService;
        this.termsService = termsService;
        this.currentUser = currentUser;
        this.auditService = auditService;
        this.clock = clock;
        this.dummyPasswordHash = passwordEncoder.encode("hash-ficticio-para-tempo-constante-1");
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException();
        }
        String termsVersion = termsService.currentVersion();
        User user = userRepository.save(new User(request.name().trim(), email,
                passwordEncoder.encode(request.password()), Role.STUDENT,
                LocalDateTime.now(clock), termsVersion));
        auditService.record(AuditAction.USER_REGISTERED, user.getId(), user.getEmail(),
                "Declarou ter 16 anos ou mais e aceitou o Termo de Uso e a Política de Privacidade v" + termsVersion,
                true);
        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());
        if (auditService.countLoginFailuresSinceLastSuccess(email, BLOCK_WINDOW) >= MAX_FAILED_ATTEMPTS) {
            auditService.record(AuditAction.LOGIN_BLOCKED, null, email,
                    "Bloqueado por " + BLOCK_WINDOW.toMinutes() + " min após " + MAX_FAILED_ATTEMPTS
                            + " tentativas sem sucesso", false);
            throw new TooManyLoginAttemptsException(BLOCK_WINDOW.toMinutes());
        }
        Optional<User> user = userRepository.findByEmailIgnoreCase(email);
        boolean passwordMatches = passwordEncoder.matches(request.password(),
                user.map(User::getPasswordHash).orElse(dummyPasswordHash));
        if (user.isEmpty() || !passwordMatches) {
            auditService.record(AuditAction.LOGIN_FAILURE, user.map(User::getId).orElse(null), email,
                    user.isEmpty() ? "E-mail não cadastrado" : "Senha incorreta", false);
            throw new InvalidCredentialsException();
        }
        auditService.record(AuditAction.LOGIN_SUCCESS, user.get().getId(), email, null, true);
        return authResponse(user.get());
    }

    /** Revoga o token usado na requisição: ele para de valer na hora, mesmo antes de expirar. */
    public void logout(Jwt token) {
        tokenRevocationService.revoke(token);
        auditService.record(AuditAction.LOGOUT, Long.valueOf(token.getSubject()),
                token.getClaimAsString(TokenService.EMAIL_CLAIM), null, true);
    }

    /** Novo aceite da versão vigente do Termo/Política. Devolve um token novo, já com a versão aceita. */
    @Transactional
    public AuthResponse acceptTerms(AcceptTermsRequest request) {
        User user = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        String version = request.version().trim();
        if (!termsService.isCurrent(version)) {
            throw new BusinessRuleException("A versão " + version + " não é a vigente. Recarregue a página.");
        }
        user.acceptTerms(version, LocalDateTime.now(clock));
        auditService.record(AuditAction.TERMS_ACCEPTED, user.getId(), user.getEmail(),
                "Aceitou o Termo de Uso e a Política de Privacidade v" + version, true);
        return authResponse(user);
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(tokenService.generate(user), "Bearer", tokenService.getExpirationSeconds(),
                UserResponse.from(user),
                termsService.isCurrent(user.getTermsVersion()) ? null : termsService.currentResponse());
    }

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
