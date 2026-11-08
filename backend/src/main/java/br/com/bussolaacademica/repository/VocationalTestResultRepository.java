package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.VocationalTestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VocationalTestResultRepository extends JpaRepository<VocationalTestResult, Long> {

    List<VocationalTestResult> findByUser_IdOrderByCreatedAtDesc(Long userId);

    void deleteByUser_Id(Long userId);

    List<VocationalTestResult> findByCreatedAtGreaterThanEqual(LocalDateTime start);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);
}
