package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.AdmissionMethod;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/** Forma de ingresso com o conteúdo do acordeão; "howItWorks" vem como lista de passos. */
public record AdmissionMethodResponse(Long id, String name, String type, String typeLabel, String summary,
                                      String description, List<String> howItWorks, String eligibility,
                                      String schedule, String cost, String tips, String officialUrl,
                                      boolean active, LocalDateTime updatedAt) {

    public static AdmissionMethodResponse from(AdmissionMethod method) {
        List<String> steps = Arrays.stream(method.getHowItWorks().split("\\R"))
                .map(String::trim)
                .filter(step -> !step.isEmpty())
                .toList();
        return new AdmissionMethodResponse(method.getId(), method.getName(), method.getType().name(),
                method.getType().getLabel(), method.getSummary(), method.getDescription(), steps,
                method.getEligibility(), method.getSchedule(), method.getCost(), method.getTips(),
                method.getOfficialUrl(), method.isActive(), method.getUpdatedAt());
    }
}
