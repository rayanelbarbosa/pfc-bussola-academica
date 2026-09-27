package br.com.bussolaacademica.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Formato padrão de erro devolvido pela API (mensagem em português, exibida ao usuário). */
public record ErrorResponse(int status, String message, List<String> details, LocalDateTime timestamp) {

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, List.of(), LocalDateTime.now());
    }

    public static ErrorResponse of(int status, String message, List<String> details) {
        return new ErrorResponse(status, message, details, LocalDateTime.now());
    }
}
