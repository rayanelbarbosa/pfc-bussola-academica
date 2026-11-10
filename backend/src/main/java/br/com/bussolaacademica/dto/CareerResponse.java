package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.Career;

import java.time.LocalDateTime;

/** Carreira na tela de gestão do administrador. */
public record CareerResponse(Integer id, String name, String area, String areaLabel, String primaryCategory,
                             String primaryCategoryLabel, String secondaryCategory, String secondaryCategoryLabel,
                             String code, String description, boolean active, LocalDateTime updatedAt) {

    public static CareerResponse from(Career career) {
        return new CareerResponse(career.getId(), career.getName(), career.getArea().name(),
                career.getArea().getLabel(), career.getPrimaryCategory().name(),
                career.getPrimaryCategory().getLabel(), career.getSecondaryCategory().name(),
                career.getSecondaryCategory().getLabel(), career.getCode(), career.getDescription(),
                career.isActive(), career.getUpdatedAt());
    }
}
