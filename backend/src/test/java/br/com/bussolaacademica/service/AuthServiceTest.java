package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.AcceptTermsRequest;
import br.com.bussolaacademica.dto.AuthResponse;
import br.com.bussolaacademica.dto.LoginRequest;
import br.com.bussolaacademica.dto.RegisterRequest;
import br.com.bussolaacademica.dto.TermsVersionResponse;
import br.com.bussolaacademica.exception.BusinessRuleException;
import br.com.bussolaacademica.exception.EmailAlreadyUsedException;
import br.com.bussolaacademica.exception.InvalidCredentialsException;
import br.com.bussolaacademica.exception.TooManyLoginAttemptsException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.security.CurrentUser;
import br.com.bussolaacademica.security.TokenRevocationService;
import br.com.bussolaacademica.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testes unitários das regras de cadastro, login, logout e novo aceite dos termos (sem Spring e sem banco). */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String DUMMY_HASH = "hash-ficticio";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;
    @Mock
    private TokenRevocationService tokenRevocationService;
    @Mock
    private TermsService termsService;
    @Mock
    private CurrentUser currentUser;
    @Mock
    private AuditService auditService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-10-08T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        when(passwordEncoder.encode(anyString())).thenReturn(DUMMY_HASH);
        lenient().when(termsService.currentVersion()).thenReturn("1.0");
        lenient().when(termsService.isCurrent("1.0")).thenReturn(true);
        authService = new AuthService(userRepository, passwordEncoder, tokenService, tokenRevocationService,
                termsService, currentUser, auditService, fixedClock);
    }

    private static User aluna(String termsVersion) {
        User user = new User("Aluna", "aluna@email.com", "hash-bcrypt", Role.STUDENT,
                LocalDateTime.of(2026, 9, 1, 10, 0), termsVersion);
        ReflectionTestUtils.setField(user, "id", 7L);
        return user;
    }

    private void stubToken() {
        when(tokenService.generate(any(User.class))).thenReturn("jwt-gerado");
        when(tokenService.getExpirationSeconds()).thenReturn(7200L);
    }

    @Test
    void deveCadastrarEstudanteComSenhaCriptografadaEEmailNormalizado() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("aluna@email.com")).thenReturn(false);
        when(passwordEncoder.encode("Senha1234")).thenReturn("hash-bcrypt");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        stubToken();

        // Act
        AuthResponse response = authService.register(
                new RegisterRequest("  Aluna  ", "  Aluna@Email.COM ", "Senha1234", true, true));

        // Assert
        ArgumentCaptor<User> salvo = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(salvo.capture());
        assertEquals("Aluna", salvo.getValue().getName());
        assertEquals("aluna@email.com", salvo.getValue().getEmail());
        assertEquals("hash-bcrypt", salvo.getValue().getPasswordHash());
        assertEquals(Role.STUDENT, salvo.getValue().getRole());
        assertEquals("1.0", salvo.getValue().getTermsVersion());
        assertEquals("jwt-gerado", response.token());
        assertNull(response.pendingTerms());
        verify(auditService).record(eq(AuditAction.USER_REGISTERED), isNull(), eq("aluna@email.com"),
                anyString(), eq(true));
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaEstiverCadastrado() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("aluna@email.com")).thenReturn(true);
        RegisterRequest request = new RegisterRequest("Aluna", "aluna@email.com", "Senha1234", true, true);

        // Act
        EmailAlreadyUsedException ex = assertThrows(EmailAlreadyUsedException.class,
                () -> authService.register(request));

        // Assert
        assertEquals("Já existe uma conta com este e-mail.", ex.getMessage());
        verify(passwordEncoder, never()).encode("Senha1234");
        verify(userRepository, never()).save(any());
    }

    @Test
    void deveRecusarLoginComSenhaIncorretaERegistrarFalha() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("aluna@email.com")).thenReturn(Optional.of(aluna("1.0")));
        when(passwordEncoder.matches("SenhaErrada1", "hash-bcrypt")).thenReturn(false);
        LoginRequest request = new LoginRequest("aluna@email.com", "SenhaErrada1");

        // Act
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));

        // Assert
        assertEquals("E-mail ou senha inválidos.", ex.getMessage());
        verify(auditService).record(AuditAction.LOGIN_FAILURE, 7L, "aluna@email.com", "Senha incorreta", false);
        verify(tokenService, never()).generate(any());
    }

    @Test
    void deveCompararComHashFicticioQuandoEmailNaoEstiverCadastrado() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("ninguem@email.com")).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest("ninguem@email.com", "Senha1234");

        // Act
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));

        // Assert: mesma mensagem e o BCrypt roda do mesmo jeito (tempo de resposta igual ao de senha errada)
        assertEquals("E-mail ou senha inválidos.", ex.getMessage());
        verify(passwordEncoder).matches("Senha1234", DUMMY_HASH);
        verify(auditService).record(AuditAction.LOGIN_FAILURE, null, "ninguem@email.com",
                "E-mail não cadastrado", false);
    }

    @Test
    void deveBloquearLoginDepoisDeCincoFalhasEmQuinzeMinutos() {
        // Arrange
        when(auditService.countLoginFailuresSinceLastSuccess("aluna@email.com", Duration.ofMinutes(15)))
                .thenReturn(5L);
        LoginRequest request = new LoginRequest("aluna@email.com", "Senha1234");

        // Act
        TooManyLoginAttemptsException ex = assertThrows(TooManyLoginAttemptsException.class,
                () -> authService.login(request));

        // Assert: nem chega a conferir a senha (mesmo a certa)
        assertEquals("Muitas tentativas de login sem sucesso. Aguarde 15 minutos e tente novamente.", ex.getMessage());
        verify(userRepository, never()).findByEmailIgnoreCase(anyString());
        verify(auditService).record(eq(AuditAction.LOGIN_BLOCKED), isNull(), eq("aluna@email.com"),
                anyString(), eq(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"aluna@email.com", "ALUNA@EMAIL.COM", "  Aluna@Email.com  "})
    void deveAutenticarIndependenteDeMaiusculasEEspacosNoEmail(String emailDigitado) {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("aluna@email.com")).thenReturn(Optional.of(aluna("1.0")));
        when(passwordEncoder.matches("Senha1234", "hash-bcrypt")).thenReturn(true);
        stubToken();

        // Act
        AuthResponse response = authService.login(new LoginRequest(emailDigitado, "Senha1234"));

        // Assert
        assertEquals("jwt-gerado", response.token());
        assertEquals("aluna@email.com", response.user().email());
        verify(auditService).record(AuditAction.LOGIN_SUCCESS, 7L, "aluna@email.com", null, true);
    }

    @Test
    void deveAvisarNoLoginQuandoExisteVersaoNovaDosTermos() {
        // Arrange
        TermsVersionResponse nova = new TermsVersionResponse("1.1", "Novo prazo de guarda dos logs",
                "Texto do termo", "Texto da política", LocalDateTime.of(2026, 10, 1, 9, 0), "admin@email.com");
        when(userRepository.findByEmailIgnoreCase("aluna@email.com")).thenReturn(Optional.of(aluna("1.0")));
        when(passwordEncoder.matches("Senha1234", "hash-bcrypt")).thenReturn(true);
        when(termsService.isCurrent("1.0")).thenReturn(false);
        when(termsService.currentResponse()).thenReturn(nova);
        stubToken();

        // Act
        AuthResponse response = authService.login(new LoginRequest("aluna@email.com", "Senha1234"));

        // Assert
        assertEquals(nova, response.pendingTerms());
    }

    @Test
    void deveRegistrarNovoAceiteDaVersaoVigente() {
        // Arrange
        User user = aluna("1.0");
        when(currentUser.id()).thenReturn(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(termsService.isCurrent("1.1")).thenReturn(true);
        stubToken();

        // Act
        AuthResponse response = authService.acceptTerms(new AcceptTermsRequest("1.1"));

        // Assert
        assertEquals("1.1", user.getTermsVersion());
        assertEquals(LocalDateTime.of(2026, 10, 8, 12, 0), user.getTermsAcceptedAt());
        assertNotNull(response.token());
        verify(auditService).record(eq(AuditAction.TERMS_ACCEPTED), eq(7L), eq("aluna@email.com"),
                anyString(), eq(true));
    }

    @Test
    void deveRecusarAceiteDeVersaoQueNaoEVigente() {
        // Arrange
        when(currentUser.id()).thenReturn(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(aluna("1.0")));
        when(termsService.isCurrent("0.9")).thenReturn(false);

        // Act + Assert
        assertThrows(BusinessRuleException.class, () -> authService.acceptTerms(new AcceptTermsRequest("0.9")));
        verify(tokenService, never()).generate(any());
    }

    @Test
    void deveRevogarTokenNoLogout() {
        // Arrange
        Jwt token = Jwt.withTokenValue("jwt").header("alg", "HS256").subject("7").jti("id-do-token")
                .claim(TokenService.EMAIL_CLAIM, "aluna@email.com").build();

        // Act
        authService.logout(token);

        // Assert
        verify(tokenRevocationService).revoke(token);
        verify(auditService).record(AuditAction.LOGOUT, 7L, "aluna@email.com", null, true);
    }
}
