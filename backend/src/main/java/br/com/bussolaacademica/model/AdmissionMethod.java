package br.com.bussolaacademica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Forma de ingresso no ensino superior (Enem, Sisu, Prouni, Fies, vestibulares, portais de bolsas),
 * com o conteúdo explicativo exibido na página inicial. Não é excluída: o administrador a desativa.
 */
@Entity
@Table(name = "admission_method")
public class AdmissionMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private AdmissionType type;

    @Column(nullable = false, length = 300)
    private String summary;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "how_it_works", nullable = false, length = 1500)
    private String howItWorks;

    @Column(nullable = false, length = 1000)
    private String eligibility;

    @Column(nullable = false, length = 500)
    private String schedule;

    @Column(nullable = false, length = 500)
    private String cost;

    @Column(length = 1000)
    private String tips;

    @Column(name = "official_url", length = 300)
    private String officialUrl;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AdmissionMethod() {
        // exigido pelo JPA
    }

    public AdmissionMethod(AdmissionMethodContent content, boolean active, int displayOrder, LocalDateTime updatedAt) {
        this.displayOrder = displayOrder;
        update(content, active, updatedAt);
    }

    public void update(AdmissionMethodContent content, boolean active, LocalDateTime updatedAt) {
        this.name = content.name();
        this.type = content.type();
        this.summary = content.summary();
        this.description = content.description();
        this.howItWorks = content.howItWorks();
        this.eligibility = content.eligibility();
        this.schedule = content.schedule();
        this.cost = content.cost();
        this.tips = content.tips();
        this.officialUrl = content.officialUrl();
        this.active = active;
        this.updatedAt = updatedAt;
    }

    public void changeStatus(boolean active, LocalDateTime updatedAt) {
        this.active = active;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public AdmissionType getType() { return type; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String getHowItWorks() { return howItWorks; }
    public String getEligibility() { return eligibility; }
    public String getSchedule() { return schedule; }
    public String getCost() { return cost; }
    public String getTips() { return tips; }
    public String getOfficialUrl() { return officialUrl; }
    public boolean isActive() { return active; }
    public int getDisplayOrder() { return displayOrder; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
