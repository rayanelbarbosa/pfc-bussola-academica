package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.AuthResponse;
import br.com.bussolaacademica.dto.ForgotPasswordRequest;
import br.com.bussolaacademica.dto.MessageResponse;
import br.com.bussolaacademica.dto.ResetPasswordRequest;
import br.com.bussolaacademica.dto.ResetTokenRequest;
import br.com.bussolaacademica.service.PasswordResetService;
import br.com.bussolaacademica.dto.LoginRequest;
import br.com.bussolaacademica.dto.RegisterRequest;
import br.com.bussolaacademica.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Cadastro, login e recuperação de senha (rotas públicas) e logout (exige o token, que é revogado). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    static final String FORGOT_PASSWORD_MESSAGE =
            "Se o e-mail estiver cadastrado, você vai receber em alguns minutos um link para criar uma nova senha.";

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Sempre a mesma resposta, exista ou não conta com o e-mail (não revela quem é cadastrado). */
    @PostMapping("/password/forgot")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request);
        return new MessageResponse(FORGOT_PASSWORD_MESSAGE);
    }

    /** 204 se o link ainda vale; 422 se é inválido, já foi usado ou expirou. */
    @PostMapping("/password/reset/validate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validateResetToken(@Valid @RequestBody ResetTokenRequest request) {
        passwordResetService.validateToken(request.token());
    }

    @PostMapping("/password/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Jwt token) {
        authService.logout(token);
    }
}
