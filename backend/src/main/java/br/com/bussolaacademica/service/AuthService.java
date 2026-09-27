package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.AuthResponse;
import br.com.bussolaacademica.dto.LoginRequest;
import br.com.bussolaacademica.dto.RegisterRequest;
import br.com.bussolaacademica.dto.UserResponse;
import br.com.bussolaacademica.exception.EmailAlreadyUsedException;
import br.com.bussolaacademica.exception.InvalidCredentialsException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

/** Cadastro e login. Todo cadastro novo recebe o perfil STUDENT. */
@Service
public class AuthService {

    /** Versão vigente do Termo de Uso e da Política de Privacidade aceitos no cadastro. */
    public static final String CURRENT_TERMS_VERSION = "1.0";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuditService auditService;
    private final Clock clock;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService,
                       AuditService auditService, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException();
        }
        User user = userRepository.save(new User(request.name().trim(), email,
                passwordEncoder.encode(request.password()), Role.STUDENT,
                LocalDateTime.now(clock), CURRENT_TERMS_VERSION));
        auditService.record(AuditAction.USER_REGISTERED, user.getId(), user.getEmail(),
                "Aceite dos Termos de Uso e da Política de Privacidade v" + CURRENT_TERMS_VERSION, true);
        return authResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());
        Optional<User> user = userRepository.findByEmailIgnoreCase(email);
        if (user.isEmpty() || !passwordEncoder.matches(request.password(), user.get().getPasswordHash())) {
            auditService.record(AuditAction.LOGIN_FAILURE, user.map(User::getId).orElse(null), email,
                    user.isEmpty() ? "E-mail não cadastrado" : "Senha incorreta", false);
            throw new InvalidCredentialsException();
        }
        auditService.record(AuditAction.LOGIN_SUCCESS, user.get().getId(), email, null, true);
        return authResponse(user.get());
    }

    private AuthResponse authResponse(User user) {
        return new AuthResponse(tokenService.generate(user), "Bearer", tokenService.getExpirationSeconds(),
                UserResponse.from(user));
    }

    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
