package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.Career;

import java.util.ArrayList;
import java.util.List;

/**
 * Carreira recomendada: área, código RIASEC (ex.: "SIC") e os tipos do código, na ordem (o 1º é o principal).
 * {@code dailyTasks}: até 3 tarefas do dia a dia (O*NET), para o "Mais sobre a carreira".
 */
public record CareerRecommendationResponse(String name, String area, String areaLabel, String code,
                                           List<CategoryLabel> categories, String description, List<String> dailyTasks) {

    public record CategoryLabel(String category, String label) {
    }

    public static CareerRecommendationResponse from(Career career) {
        List<CategoryLabel> categories = new ArrayList<>(List.of(
                new CategoryLabel(career.getPrimaryCategory().name(), career.getPrimaryCategory().getLabel()),
                new CategoryLabel(career.getSecondaryCategory().name(), career.getSecondaryCategory().getLabel())));
        if (career.getTertiaryCategory() != null) {
            categories.add(new CategoryLabel(career.getTertiaryCategory().name(), career.getTertiaryCategory().getLabel()));
        }
        return new CareerRecommendationResponse(career.getName(), career.getArea().name(), career.getArea().getLabel(),
                career.getCode(), List.copyOf(categories), career.getDescription(), career.getDailyTasks());
    }
}
