package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.VocationalTestResult;

import java.time.LocalDateTime;
import java.util.List;

/** Resumo de um resultado para o histórico do usuário. */
public record ResultSummaryResponse(Long id, LocalDateTime createdAt, List<String> topCategories) {

    public static ResultSummaryResponse from(VocationalTestResult result) {
        return new ResultSummaryResponse(result.getId(), result.getCreatedAt(),
                result.getTopCategories().stream().map(RiasecCategory::getLabel).toList());
    }
}
