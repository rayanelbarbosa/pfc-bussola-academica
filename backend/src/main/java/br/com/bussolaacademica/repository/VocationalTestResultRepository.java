package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.VocationalTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VocationalTestResultRepository extends JpaRepository<VocationalTestResult, Long> {

    List<VocationalTestResult> findByUser_IdOrderByCreatedAtDesc(Long userId);

    void deleteByUser_Id(Long userId);
}
