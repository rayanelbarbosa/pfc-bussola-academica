package br.com.bussolaacademica.service;

import br.com.bussolaacademica.security.TokenRevocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Period;

/**
 * Rotina diária (3h30, horário de Brasília) que apaga dados cujo prazo de guarda acabou:
 * <ul>
 *   <li>logs de auditoria com mais de 6 meses (o Marco Civil da Internet, art. 15, exige guardar
 *       registros de acesso por 6 meses; depois disso, pela LGPD, não há motivo para mantê-los);</li>
 *   <li>tokens revogados que já expiraram;</li>
 *   <li>pedidos de redefinição de senha vencidos.</li>
 * </ul>
 */
@Component
public class DataRetentionJob {

    static final Period AUDIT_RETENTION = Period.ofMonths(6);
    private static final Logger log = LoggerFactory.getLogger(DataRetentionJob.class);

    private final AuditService auditService;
    private final TokenRevocationService tokenRevocationService;
    private final PasswordResetService passwordResetService;

    public DataRetentionJob(AuditService auditService, TokenRevocationService tokenRevocationService,
                            PasswordResetService passwordResetService) {
        this.auditService = auditService;
        this.tokenRevocationService = tokenRevocationService;
        this.passwordResetService = passwordResetService;
    }

    @Scheduled(cron = "${app.retention.cron:0 30 3 * * *}", zone = "America/Sao_Paulo")
    public void purgeExpiredData() {
        int logs = auditService.purgeOlderThan(AUDIT_RETENTION);
        int tokens = tokenRevocationService.purgeExpired();
        int resets = passwordResetService.purgeExpired();
        log.info("Limpeza diária: {} logs de auditoria, {} tokens revogados e {} pedidos de redefinição apagados",
                logs, tokens, resets);
    }
}
