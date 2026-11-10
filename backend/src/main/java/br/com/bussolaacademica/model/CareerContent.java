package br.com.bussolaacademica.model;

/** Conteúdo editável de uma carreira (já validado e sem espaços sobrando). */
public record CareerContent(String name, InterestArea area, RiasecCategory primaryCategory,
                            RiasecCategory secondaryCategory, RiasecCategory tertiaryCategory, String description) {
}
