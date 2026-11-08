package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.AcceptTermsRequest;
import br.com.bussolaacademica.dto.AuthResponse;
import br.com.bussolaacademica.dto.TermsVersionResponse;
import br.com.bussolaacademica.service.AuthService;
import br.com.bussolaacademica.service.TermsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Versão vigente do Termo de Uso e da Política de Privacidade (pública) e novo aceite (usuário logado). */
@RestController
@RequestMapping("/api/terms")
public class TermsController {

    private final TermsService termsService;
    private final AuthService authService;

    public TermsController(TermsService termsService, AuthService authService) {
        this.termsService = termsService;
        this.authService = authService;
    }

    @GetMapping("/current")
    public TermsVersionResponse current() {
        return termsService.currentResponse();
    }

    @PostMapping("/accept")
    public AuthResponse accept(@Valid @RequestBody AcceptTermsRequest request) {
        return authService.acceptTerms(request);
    }
}
