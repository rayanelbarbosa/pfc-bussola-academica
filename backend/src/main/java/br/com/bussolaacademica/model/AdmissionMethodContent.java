package br.com.bussolaacademica.model;

/** Conteúdo editável de uma forma de ingresso (já validado e sem espaços sobrando). */
public record AdmissionMethodContent(String name, AdmissionType type, String summary, String description,
                                     String howItWorks, String eligibility, String schedule, String cost,
                                     String tips, String officialUrl) {
}
