package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.service.CareerRecommender.Pick;

import java.util.ArrayList;
import java.util.List;

/**
 * Carreira recomendada: área, código RIASEC (ex.: "RIC"), tipos do código e pontuação de compatibilidade.
 * Quando empatou com a carreira seguinte do ranking, {@code tiedWith} traz o nome dela e {@code tieReason} o critério
 * que deixou esta na frente (PROFILE, THIRD_TYPE ou NAME); sem empate, os dois são nulos.
 */
public record CareerRecommendationResponse(String name, String area, String areaLabel, String code,
                                           List<CategoryLabel> categories, int compatibility, String description,
                                           String tiedWith, String tieReason) {

    public record CategoryLabel(String category, String label) {
    }

    public static CareerRecommendationResponse from(Pick pick) {
        var ranked = pick.ranked();
        var career = ranked.career();
        List<CategoryLabel> categories = new ArrayList<>(List.of(
                new CategoryLabel(career.getPrimaryCategory().name(), career.getPrimaryCategory().getLabel()),
                new CategoryLabel(career.getSecondaryCategory().name(), career.getSecondaryCategory().getLabel())));
        if (career.getTertiaryCategory() != null) {
            categories.add(new CategoryLabel(career.getTertiaryCategory().name(), career.getTertiaryCategory().getLabel()));
        }
        return new CareerRecommendationResponse(career.getName(), career.getArea().name(), career.getArea().getLabel(),
                career.getCode(), List.copyOf(categories),
                ranked.compatibility(), career.getDescription(),
                pick.aheadOf() == null ? null : pick.aheadOf().career().getName(),
                pick.reason() == null ? null : pick.reason().name());
    }
}
