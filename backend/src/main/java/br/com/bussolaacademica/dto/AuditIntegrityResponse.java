package br.com.bussolaacademica.dto;

import java.time.LocalDateTime;

/**
 * Resultado da verificação da corrente de auditoria: se está íntegra, quantos registros lacrados foram conferidos,
 * o id do primeiro registro com problema (nulo quando íntegra) e uma explicação para o administrador.
 */
public record AuditIntegrityResponse(boolean intact, long checkedRecords, Long brokenAtId, String message,
                                     LocalDateTime checkedAt) {
}
