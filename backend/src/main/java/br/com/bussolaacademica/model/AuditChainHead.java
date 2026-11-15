package br.com.bussolaacademica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Último hash da corrente de auditoria (uma linha só, id = 1). */
@Entity
@Table(name = "audit_chain_head")
public class AuditChainHead {

    public static final int ID = 1;

    @Id
    private Integer id;

    @Column(name = "last_hash", length = 64)
    private String lastHash;

    protected AuditChainHead() {
        // exigido pelo JPA
    }

    public String getLastHash() {
        return lastHash;
    }

    /** Avança a corrente: o registro recém-gravado passa a ser o último. */
    public void advance(String hash) {
        this.lastHash = hash;
    }
}
