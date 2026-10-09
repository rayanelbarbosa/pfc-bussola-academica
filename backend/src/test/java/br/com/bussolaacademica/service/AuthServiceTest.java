package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.AuthResponse;
import br.com.bussolaacademica.dto.LoginRequest;
import br.com.bussolaacademica.dto.RegisterRequest;
import br.com.bussolaacademica.exception.EmailAlreadyUsedException;
import br.com.bussolaacademica.exception.InvalidCredentialsException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.repository.UserRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testes unitários das regras de cadastro e login (sem Spring e sem banco). */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;
    @Mock
    private AuditService auditService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-10-08T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        authService = new AuthService(userRepository, passwordEncoder, tokenService, auditService, fixedClock);
    }

    private static User aluna() {
        User user = new User("Aluna", "aluna@email.com", "hash-bcrypt", Role.STUDENT,
                LocalDateTime.of(2026, 9, 1, 10, 0), "1.0");
        ReflectionTestUtils.setField(user, "id", 7L);
        return user;
    }

    @Test
    void deveCadastrarEstudanteComSenhaCriptografadaEEmailNormalizado() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("aluna@email.com")).thenReturn(false);
        when(passwordEncoder.encode("Senha1234")).thenReturn("hash-bcrypt");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenService.generate(any(User.class))).thenReturn("jwt-gerado");
        when(tokenService.getExpirationSeconds()).thenReturn(7200L);

        // Act
        AuthResponse response = authService.register(
                new RegisterRequest("  Aluna  ", "  Aluna@Email.COM ", "Senha1234", true));

        // Assert
        ArgumentCaptor<User> salvo = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(salvo.capture());
        assertEquals("Aluna", salvo.getValue().getName());
        assertEquals("aluna@email.com", salvo.getValue().getEmail());
        assertEquals("hash-bcrypt", salvo.getValue().getPasswordHash());
        assertEquals(Role.STUDENT, salvo.getValue().getRole());
        assertEquals("1.0", salvo.getValue().getTermsVersion());
        assertEquals("jwt-gerado", response.token());
        verify(auditService).record(eq(AuditAction.USER_REGISTERED), isNull(), eq("aluna@email.com"),
                anyString(), eq(true));
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaEstiverCadastrado() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("aluna@email.com")).thenReturn(true);
        RegisterRequest request = new RegisterRequest("Aluna", "aluna@email.com", "Senha1234", true);

        // Act
        EmailAlreadyUsedException ex = assertThrows(EmailAlreadyUsedException.class,
                () -> authService.register(request));

        // Assert
        assertEquals("Já existe uma conta com este e-mail.", ex.getMessage());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deveRecusarLoginComSenhaIncorretaERegistrarFalha() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("aluna@email.com")).thenReturn(Optional.of(aluna()));
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
    void deveUsarMesmaMensagemQuandoEmailNaoEstiverCadastrado() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("ninguem@email.com")).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest("ninguem@email.com", "Senha1234");

        // Act
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));

        // Assert
        assertEquals("E-mail ou senha inválidos.", ex.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(auditService).record(AuditAction.LOGIN_FAILURE, null, "ninguem@email.com",
                "E-mail não cadastrado", false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"aluna@email.com", "ALUNA@EMAIL.COM", "  Aluna@Email.com  "})
    void deveAutenticarIndependenteDeMaiusculasEEspacosNoEmail(String emailDigitado) {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("aluna@email.com")).thenReturn(Optional.of(aluna()));
        when(passwordEncoder.matches("Senha1234", "hash-bcrypt")).thenReturn(true);
        when(tokenService.generate(any(User.class))).thenReturn("jwt-gerado");
        when(tokenService.getExpirationSeconds()).thenReturn(7200L);

        // Act
        AuthResponse response = authService.login(new LoginRequest(emailDigitado, "Senha1234"));

        // Assert
        assertEquals("jwt-gerado", response.token());
        assertEquals("aluna@email.com", response.user().email());
        verify(auditService).record(AuditAction.LOGIN_SUCCESS, 7L, "aluna@email.com", null, true);
    }
}
