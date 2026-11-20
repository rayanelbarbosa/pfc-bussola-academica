package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.VocationalTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VocationalTestResultRepository extends JpaRepository<VocationalTestResult, Long> {

    List<VocationalTestResult> findByUser_IdOrderByCreatedAtDesc(Long userId);

    /**
     * Regra de negócio (LGPD, art. 12 e art. 16, IV): ao excluir a conta, os resultados não são apagados, e sim
     * anonimizados. Deixam de pertencer ao usuário e guardam só a data, para não poderem ser religados à pessoa;
     * continuam contando nas estatísticas do painel do administrador. Precisa rodar dentro de uma transação: as
     * mudanças são gravadas pelo próprio JPA ao final dela.
     */
    default void anonymizeByUser(Long userId) {
        findByUser_IdOrderByCreatedAtDesc(userId).forEach(VocationalTestResult::anonymize);
    }

    /** Testes feitos em [start, end): o início entra, o fim não. */
    List<VocationalTestResult> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);
}
