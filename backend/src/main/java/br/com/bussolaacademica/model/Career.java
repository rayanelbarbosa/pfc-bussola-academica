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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

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

    /** 3º tipo do código de Holland: só desempata carreiras com a mesma compatibilidade (opcional). */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tertiary_category", length = 20)
    private RiasecCategory tertiaryCategory;

    /**
     * Ocupação do O*NET usada como referência (ex.: "29-1216.00") e as 6 notas de interesse dela (escala OI, 1 a 7).
     * Opcionais: carreiras cadastradas pela tela podem não ter. Usadas só para desempatar a recomendação.
     */
    @Column(name = "onet_code", length = 10)
    private String onetCode;

    @Column(name = "onet_realistic")
    private Double onetRealistic;

    @Column(name = "onet_investigative")
    private Double onetInvestigative;

    @Column(name = "onet_artistic")
    private Double onetArtistic;

    @Column(name = "onet_social")
    private Double onetSocial;

    @Column(name = "onet_enterprising")
    private Double onetEnterprising;

    @Column(name = "onet_conventional")
    private Double onetConventional;

    @Column(nullable = false, length = 300)
    private String description;

    /** Até 3 tarefas do dia a dia (O*NET), separadas por {@value #TASK_SEPARATOR}; opcional. */
    @Column(name = "daily_tasks", length = 600)
    private String dailyTasks;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static final String TASK_SEPARATOR = "|";

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

    public Career(Integer id, String name, InterestArea area, RiasecCategory primaryCategory,
                  RiasecCategory secondaryCategory, RiasecCategory tertiaryCategory, String description) {
        this(id, name, area, primaryCategory, secondaryCategory, description);
        this.tertiaryCategory = tertiaryCategory;
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
        this.tertiaryCategory = content.tertiaryCategory();
        this.description = content.description();
        this.active = active;
        this.updatedAt = updatedAt;
    }

    public void changeStatus(boolean active, LocalDateTime updatedAt) {
        this.active = active;
        this.updatedAt = updatedAt;
    }

    /** Código de Holland, ex.: "IR" ou, com o 3º tipo, "IRC" (Investigativo + Realista + Convencional). */
    public String getCode() {
        String code = primaryCategory.name().substring(0, 1) + secondaryCategory.name().substring(0, 1);
        return tertiaryCategory == null ? code : code + tertiaryCategory.name().charAt(0);
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public InterestArea getArea() { return area; }
    public RiasecCategory getPrimaryCategory() { return primaryCategory; }
    public RiasecCategory getSecondaryCategory() { return secondaryCategory; }
    public RiasecCategory getTertiaryCategory() { return tertiaryCategory; }
    public String getOnetCode() { return onetCode; }

    /** As 6 notas do O*NET na ordem R, I, A, S, E, C, ou null se a carreira não tiver o perfil completo. */
    public double[] getInterestProfile() {
        Double[] values = {onetRealistic, onetInvestigative, onetArtistic, onetSocial, onetEnterprising, onetConventional};
        double[] profile = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                return null;
            }
            profile[i] = values[i];
        }
        return profile;
    }

    /** Define o perfil do O*NET (as cargas iniciais vêm da migration V18; aqui é usado nos testes). */
    public void setInterestProfile(String onetCode, double... profile) {
        if (profile.length != 6) {
            throw new IllegalArgumentException("O perfil precisa de 6 notas (R, I, A, S, E, C)");
        }
        this.onetCode = onetCode;
        this.onetRealistic = profile[0];
        this.onetInvestigative = profile[1];
        this.onetArtistic = profile[2];
        this.onetSocial = profile[3];
        this.onetEnterprising = profile[4];
        this.onetConventional = profile[5];
    }
    public String getDescription() { return description; }

    public List<String> getDailyTasks() {
        if (dailyTasks == null || dailyTasks.isBlank()) {
            return List.of();
        }
        return Arrays.stream(dailyTasks.split(Pattern.quote(TASK_SEPARATOR))).map(String::trim)
                .filter(task -> !task.isEmpty()).toList();
    }

    /** Guarda as tarefas sem as vazias; nenhuma tarefa deixa a coluna nula. */
    public void setDailyTasks(List<String> tasks) {
        List<String> clean = tasks == null ? List.of()
                : tasks.stream().filter(Objects::nonNull).map(String::trim).filter(task -> !task.isEmpty()).toList();
        this.dailyTasks = clean.isEmpty() ? null : String.join(TASK_SEPARATOR, clean);
    }
    public boolean isActive() { return active; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
