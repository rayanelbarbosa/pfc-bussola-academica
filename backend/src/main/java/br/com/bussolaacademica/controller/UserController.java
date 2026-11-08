package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.ResultSummaryResponse;
import br.com.bussolaacademica.dto.UserDataExportResponse;
import br.com.bussolaacademica.dto.UserResponse;
import br.com.bussolaacademica.service.UserService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Dados do próprio usuário autenticado (direitos do titular na LGPD). */
@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse me() {
        return userService.me();
    }

    @GetMapping("/results")
    public List<ResultSummaryResponse> myResults() {
        return userService.myResults();
    }

    /** Baixa um arquivo JSON com todos os dados pessoais (portabilidade, LGPD art. 18, V). */
    @GetMapping("/export")
    public ResponseEntity<UserDataExportResponse> exportMyData() {
        ContentDisposition attachment = ContentDisposition.attachment()
                .filename("meus-dados-bussola-academica.json").build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, attachment.toString())
                .body(userService.exportMyData());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyAccount() {
        userService.deleteMyAccount();
    }
}
