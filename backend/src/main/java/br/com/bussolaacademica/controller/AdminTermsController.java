package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.PublishTermsRequest;
import br.com.bussolaacademica.dto.TermsVersionResponse;
import br.com.bussolaacademica.service.TermsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Administração das versões do Termo de Uso e da Política de Privacidade (somente ADMIN). */
@RestController
@RequestMapping("/api/admin/terms")
public class AdminTermsController {

    private final TermsService termsService;

    public AdminTermsController(TermsService termsService) {
        this.termsService = termsService;
    }

    @GetMapping
    public List<TermsVersionResponse> history() {
        return termsService.history();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TermsVersionResponse publish(@Valid @RequestBody PublishTermsRequest request) {
        return termsService.publish(request);
    }
}
