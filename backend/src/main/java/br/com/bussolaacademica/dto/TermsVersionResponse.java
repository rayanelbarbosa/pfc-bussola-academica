package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.TermsVersion;

import java.time.LocalDateTime;

/** Versão do Termo de Uso e da Política de Privacidade: textos completos e resumo da mudança. */
public record TermsVersionResponse(String version, String summary, String termsOfUse, String privacyPolicy,
                                   LocalDateTime publishedAt, String publishedBy) {

    public static TermsVersionResponse from(TermsVersion terms) {
        return new TermsVersionResponse(terms.getVersion(), terms.getSummary(), terms.getTermsOfUse(),
                terms.getPrivacyPolicy(), terms.getPublishedAt(), terms.getPublishedBy());
    }
}
