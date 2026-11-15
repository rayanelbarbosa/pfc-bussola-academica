package br.com.bussolaacademica.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Lacre de um registro de auditoria: SHA-256 do hash anterior + conteúdo. */
class AuditLogTest {

    private static AuditLog log(String details) {
        return new AuditLog(LocalDateTime.of(2026, 11, 15, 16, 30, 12, 123_456_789), 7L, "admin@teste.com",
                AuditAction.ACCOUNT_DELETED, "DELETE /api/admin/users/3", details, "200.1.2.3", true);
    }

    @Test
    void sealGuardaOHashAnteriorECalculaUmHashSha256DoConteudo() {
        AuditLog log = log("Conta excluída");
        log.seal("abc");

        assertThat(log.getPreviousHash()).isEqualTo("abc");
        assertThat(log.getHash()).hasSize(64).matches("[0-9a-f]+").isEqualTo(log.computeHash());
    }

    @Test
    void qualquerAlteracaoDepoisDoLacreMudaOHashRecalculado() {
        AuditLog log = log("Conta excluída");
        log.seal(null);
        String lacre = log.getHash();

        ReflectionTestUtils.setField(log, "details", "Conta mantida");

        assertThat(log.computeHash()).isNotEqualTo(lacre);
    }

    @Test
    void oMesmoConteudoComOutroHashAnteriorGeraOutroLacre() {
        AuditLog primeiro = log("Conta excluída");
        AuditLog segundo = log("Conta excluída");
        primeiro.seal(null);
        segundo.seal(primeiro.getHash());

        assertThat(segundo.getHash()).isNotEqualTo(primeiro.getHash());
    }

    @Test
    void aDataEGuardadaComMicrossegundosComoNoBanco() {
        assertThat(log("x").getCreatedAt().getNano()).isEqualTo(123_456_000);
    }
}
