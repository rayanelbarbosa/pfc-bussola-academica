package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.Career;
import br.com.bussolaacademica.model.InterestArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CareerRepository extends JpaRepository<Career, Integer>, JpaSpecificationExecutor<Career> {

    /** Carreiras usadas na recomendação: só as ativas. */
    List<Career> findByActiveTrue();

    /** Busca de vídeos: aceita só carreira ativa e cadastrada. */
    Optional<Career> findFirstByNameIgnoreCaseAndActiveTrue(String name);

    boolean existsByNameIgnoreCaseAndArea(String name, InterestArea area);

    boolean existsByNameIgnoreCaseAndAreaAndIdNot(String name, InterestArea area, Integer id);
}
