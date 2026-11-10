package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.Career;

import java.time.LocalDateTime;

/** Carreira na tela de gestão do administrador. */
public record CareerResponse(Integer id, String name, String area, String areaLabel, String primaryCategory,
                             String primaryCategoryLabel, String secondaryCategory, String secondaryCategoryLabel,
                             String tertiaryCategory, String tertiaryCategoryLabel, String code, String onetCode,
                             String description, boolean active, LocalDateTime updatedAt) {

    public static CareerResponse from(Career career) {
        return new CareerResponse(career.getId(), career.getName(), career.getArea().name(),
                career.getArea().getLabel(), career.getPrimaryCategory().name(),
                career.getPrimaryCategory().getLabel(), career.getSecondaryCategory().name(),
                career.getSecondaryCategory().getLabel(),
                career.getTertiaryCategory() == null ? null : career.getTertiaryCategory().name(),
                career.getTertiaryCategory() == null ? null : career.getTertiaryCategory().getLabel(),
                career.getCode(), career.getOnetCode(), career.getDescription(),
                career.isActive(), career.getUpdatedAt());
    }
}
