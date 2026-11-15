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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Registro de auditoria: quem fez o quê, quando, em qual rota e de qual IP.
 * Não há chave estrangeira para o usuário de propósito: o registro precisa continuar
 * existindo (rastreabilidade) mesmo que a conta seja excluída.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    /** Separador dos campos no cálculo do hash (caractere de controle "unit separator"). */
    private static final String SEPARATOR = "\u001F";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email", length = 150)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 40)
    private AuditAction action;

    @Column(length = 200)
    private String resource;

    @Column(length = 500)
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private boolean success;

    /** Hash do registro anterior da corrente (nulo no primeiro e nos registros anteriores à proteção). */
    @Column(name = "previous_hash", length = 64)
    private String previousHash;

    /** Lacre do registro: SHA-256 do hash anterior + conteúdo. Qualquer alteração deixa de bater com ele. */
    @Column(length = 64)
    private String hash;

    protected AuditLog() {
        // exigido pelo JPA
    }

    public AuditLog(LocalDateTime createdAt, Long userId, String userEmail, AuditAction action,
                    String resource, String details, String ipAddress, boolean success) {
        // o banco guarda microssegundos: truncar antes do lacre garante o mesmo hash depois de gravar e ler de novo
        this.createdAt = createdAt.truncatedTo(ChronoUnit.MICROS);
        this.userId = userId;
        this.userEmail = userEmail;
        this.action = action;
        this.resource = resource;
        this.details = details;
        this.ipAddress = ipAddress;
        this.success = success;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getResource() {
        return resource;
    }

    public String getDetails() {
        return details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isSuccess() {
        return success;
    }

    /** Lacra o registro na corrente: guarda o hash do anterior e calcula o próprio. */
    public void seal(String previous) {
        this.previousHash = previous;
        this.hash = computeHash();
    }

    /**
     * SHA-256 do hash anterior e de todos os campos do registro, separados por um caractere que não aparece no texto.
     * Recalculado na verificação: se o conteúdo ou o hash anterior mudou, o resultado não bate com {@link #getHash()}.
     */
    public String computeHash() {
        String content = String.join(SEPARATOR, Objects.toString(previousHash, ""), createdAt.toString(),
                Objects.toString(userId, ""), Objects.toString(userEmail, ""), action.name(),
                Objects.toString(resource, ""), Objects.toString(details, ""), Objects.toString(ipAddress, ""),
                Boolean.toString(success));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }
}
