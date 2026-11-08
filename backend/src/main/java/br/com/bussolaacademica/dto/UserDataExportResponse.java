package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.VocationalTestResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Exportação dos dados pessoais do titular em JSON (LGPD, art. 18, II e V: acesso e portabilidade):
 * cadastro, todos os resultados do teste e o histórico de ações registradas na auditoria.
 * O histórico traz só o que é dado do titular (quando, o quê, de qual IP e se deu certo); campos internos
 * como ids, e-mail repetido em cada linha e a rota da API ficam de fora, para o arquivo ser legível.
 */
public record UserDataExportResponse(
        LocalDateTime exportedAt,
        UserResponse account,
        List<ExportedTestResult> vocationalTestResults,
        List<ExportedActivity> activityLog
) {

    /** Uma ação do histórico, como o titular a entende. */
    public record ExportedActivity(LocalDateTime date, String action, String ipAddress, boolean success) {

        public static ExportedActivity from(AuditLogResponse log) {
            return new ExportedActivity(log.createdAt(), AuditAction.valueOf(log.action()).getLabel(),
                    log.ipAddress(), log.success());
        }
    }

    public record ExportedTestResult(Long id, LocalDateTime createdAt, List<CategoryScoreResponse> scores,
                                     List<String> topCategories,
                                     List<String> interestAreas) {

        public static ExportedTestResult from(VocationalTestResult result) {
            return new ExportedTestResult(result.getId(), result.getCreatedAt(),
                    result.getScores().entrySet().stream()
                            .map(entry -> CategoryScoreResponse.of(entry.getKey(), entry.getValue()))
                            .toList(),
                    result.getTopCategories().stream().map(RiasecCategory::getLabel).toList(),
                    result.getInterestAreas().stream().map(InterestArea::getLabel).toList());
        }
    }
}
