package br.com.bussolaacademica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Carreira (curso de graduação) de uma área de interesse, com o código RIASEC de 2 letras
 * inspirado no O*NET: a 1ª letra é o tipo dominante da profissão e a 2ª, o tipo secundário.
 */
@Entity
@Table(name = "career")
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "career_seq")
    @SequenceGenerator(name = "career_seq", sequenceName = "career_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private InterestArea area;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "primary_category", nullable = false, length = 20)
    private RiasecCategory primaryCategory;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "secondary_category", nullable = false, length = 20)
    private RiasecCategory secondaryCategory;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Career() {
        // exigido pelo JPA
    }

    public Career(Integer id, String name, InterestArea area, RiasecCategory primaryCategory,
                  RiasecCategory secondaryCategory, String description) {
        this.id = id;
        this.name = name;
        this.area = area;
        this.primaryCategory = primaryCategory;
        this.secondaryCategory = secondaryCategory;
        this.description = description;
    }

    /** Nova carreira cadastrada pelo administrador (o id vem da sequence career_seq). */
    public Career(CareerContent content, boolean active, LocalDateTime updatedAt) {
        update(content, active, updatedAt);
    }

    public void update(CareerContent content, boolean active, LocalDateTime updatedAt) {
        this.name = content.name();
        this.area = content.area();
        this.primaryCategory = content.primaryCategory();
        this.secondaryCategory = content.secondaryCategory();
        this.description = content.description();
        this.active = active;
        this.updatedAt = updatedAt;
    }

    public void changeStatus(boolean active, LocalDateTime updatedAt) {
        this.active = active;
        this.updatedAt = updatedAt;
    }

    /** Código de 2 letras, ex.: "IR" (Investigativo + Realista). */
    public String getCode() {
        return primaryCategory.name().substring(0, 1) + secondaryCategory.name().substring(0, 1);
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public InterestArea getArea() { return area; }
    public RiasecCategory getPrimaryCategory() { return primaryCategory; }
    public RiasecCategory getSecondaryCategory() { return secondaryCategory; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
