package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.TermsVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermsVersionRepository extends JpaRepository<TermsVersion, Long> {

    Optional<TermsVersion> findFirstByOrderByPublishedAtDescIdDesc();

    List<TermsVersion> findAllByOrderByPublishedAtDescIdDesc();

    boolean existsByVersionIgnoreCase(String version);
}
