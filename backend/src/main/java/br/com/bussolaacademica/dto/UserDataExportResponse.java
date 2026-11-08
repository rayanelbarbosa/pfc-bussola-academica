package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.VocationalTestResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Exportação dos dados pessoais do titular em JSON (LGPD, art. 18, II e V: acesso e portabilidade):
 * cadastro, todos os resultados do teste e o histórico de ações registradas na auditoria.
 */
public record UserDataExportResponse(
        LocalDateTime exportedAt,
        UserResponse account,
        List<ExportedTestResult> vocationalTestResults,
        List<AuditLogResponse> activityLog
) {

    public record ExportedTestResult(Long id, LocalDateTime createdAt, List<CategoryScoreResponse> scores,
                                     List<String> topCategories, List<String> recommendedAreas,
                                     List<String> interestAreas) {

        public static ExportedTestResult from(VocationalTestResult result) {
            return new ExportedTestResult(result.getId(), result.getCreatedAt(),
                    result.getScores().entrySet().stream()
                            .map(entry -> CategoryScoreResponse.of(entry.getKey(), entry.getValue()))
                            .toList(),
                    result.getTopCategories().stream().map(RiasecCategory::getLabel).toList(),
                    result.getRecommendedAreas(),
                    result.getInterestAreas().stream().map(InterestArea::getLabel).toList());
        }
    }
}
