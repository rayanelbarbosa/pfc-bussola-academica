package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.AdmissionMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdmissionMethodRepository extends JpaRepository<AdmissionMethod, Long>,
        JpaSpecificationExecutor<AdmissionMethod> {

    List<AdmissionMethod> findByActiveTrueOrderByDisplayOrderAscNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("select coalesce(max(a.displayOrder), 0) from AdmissionMethod a")
    int findMaxDisplayOrder();
}
