package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.AuditChainHead;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface AuditChainHeadRepository extends JpaRepository<AuditChainHead, Integer> {

    /**
     * Lê o último hash travando a linha (SELECT ... FOR UPDATE) até o fim da transação: uma segunda gravação
     * simultânea espera a primeira terminar e já lê o hash novo.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from AuditChainHead h where h.id = " + AuditChainHead.ID)
    AuditChainHead lockHead();

    /**
     * Lê o último hash para a verificação, também travando: assim nenhum registro novo entra entre a leitura da
     * corrente e a comparação com o último hash.
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select h from AuditChainHead h where h.id = " + AuditChainHead.ID)
    AuditChainHead lockHeadForRead();
}
