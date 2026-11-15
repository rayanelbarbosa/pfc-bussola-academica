package br.com.bussolaacademica.integration;

import br.com.bussolaacademica.dto.AuditIntegrityResponse;
import br.com.bussolaacademica.model.AuditLog;
import br.com.bussolaacademica.repository.AuditLogRepository;
import br.com.bussolaacademica.service.AuditIntegrityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_EMAIL;
import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.bearer;
import static br.com.bussolaacademica.support.AuthTestHelper.login;
import static br.com.bussolaacademica.support.AuthTestHelper.registerStudent;
import static br.com.bussolaacademica.support.AuthTestHelper.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Proteção da auditoria: a corrente de hashes detecta registro alterado, apagado ou inserido por fora. */
@SpringBootTest
@AutoConfigureMockMvc
class AuditIntegrityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditIntegrityService integrityService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void adminVerifiesTheChainAndStudentIsBlocked() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        String student = registerStudent(mockMvc, uniqueEmail());

        mockMvc.perform(get("/api/admin/audit-logs/integrity").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intact").value(true))
                .andExpect(jsonPath("$.brokenAtId").doesNotExist());
        mockMvc.perform(get("/api/admin/audit-logs/integrity").header("Authorization", bearer(student)))
                .andExpect(status().isForbidden());
        // a própria verificação fica registrada
        mockMvc.perform(get("/api/admin/audit-logs").param("action", "AUDIT_INTEGRITY_CHECKED")
                        .header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.content[0].action").value("AUDIT_INTEGRITY_CHECKED"));
    }

    @Test
    void detectsAChangedRecordAndAcceptsItAgainWhenRestored() throws Exception {
        login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        AuditLog sealed = lastSealed();
        String original = sealed.getDetails();

        jdbc.update("update audit_log set details = ? where id = ?", "alterado por fora", sealed.getId());
        AuditIntegrityResponse changed = integrityService.verify();

        jdbc.update("update audit_log set details = ? where id = ?", original, sealed.getId());
        AuditIntegrityResponse restored = integrityService.verify();

        assertThat(changed.intact()).isFalse();
        assertThat(changed.brokenAtId()).isEqualTo(sealed.getId());
        assertThat(changed.message()).contains("alterado");
        assertThat(restored.intact()).isTrue();
    }

    @Test
    void detectsWhenTheNewestRecordIsDeleted() throws Exception {
        login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        String head = jdbc.queryForObject("select last_hash from audit_chain_head where id = 1", String.class);

        // simula a exclusão do registro mais recente: a corrente guardada à parte não bate mais com o último registro
        jdbc.update("update audit_chain_head set last_hash = ? where id = 1", "0".repeat(64));
        AuditIntegrityResponse deleted = integrityService.verify();
        jdbc.update("update audit_chain_head set last_hash = ? where id = 1", head);

        assertThat(deleted.intact()).isFalse();
        assertThat(deleted.message()).contains("mais recente");
        assertThat(integrityService.verify().intact()).isTrue();
    }

    @Test
    void detectsARecordInsertedWithoutSeal() throws Exception {
        login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        jdbc.update("insert into audit_log (created_at, action, success) values (current_timestamp, 'LOGIN_SUCCESS', true)");
        Long inserted = jdbc.queryForObject("select max(id) from audit_log", Long.class);

        AuditIntegrityResponse result = integrityService.verify();
        auditLogRepository.deleteById(inserted);

        assertThat(result.intact()).isFalse();
        assertThat(result.brokenAtId()).isEqualTo(inserted);
        assertThat(result.message()).contains("sem lacre");
        assertThat(integrityService.verify().intact()).isTrue();
    }

    private AuditLog lastSealed() {
        return auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .filter(log -> log.getHash() != null)
                .findFirst().orElseThrow();
    }
}
