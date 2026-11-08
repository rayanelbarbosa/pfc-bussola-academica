package br.com.bussolaacademica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Versão publicada do Termo de Uso e da Política de Privacidade: o texto completo dos dois documentos
 * e o resumo do que mudou. Versões publicadas não são alteradas; uma mudança é sempre uma versão nova.
 */
@Entity
@Table(name = "terms_version")
public class TermsVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String version;

    @Column(nullable = false, length = 500)
    private String summary;

    @Column(name = "terms_of_use", nullable = false, length = 20000)
    private String termsOfUse;

    @Column(name = "privacy_policy", nullable = false, length = 20000)
    private String privacyPolicy;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "published_by", length = 150)
    private String publishedBy;

    protected TermsVersion() {
        // exigido pelo JPA
    }

    public TermsVersion(String version, String summary, String termsOfUse, String privacyPolicy,
                        LocalDateTime publishedAt, String publishedBy) {
        this.version = version;
        this.summary = summary;
        this.termsOfUse = termsOfUse;
        this.privacyPolicy = privacyPolicy;
        this.publishedAt = publishedAt;
        this.publishedBy = publishedBy;
    }

    public Long getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getSummary() {
        return summary;
    }

    public String getTermsOfUse() {
        return termsOfUse;
    }

    public String getPrivacyPolicy() {
        return privacyPolicy;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public String getPublishedBy() {
        return publishedBy;
    }
}
