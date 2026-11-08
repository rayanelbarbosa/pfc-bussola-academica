package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.Career;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareerRepository extends JpaRepository<Career, Integer> {

    Optional<Career> findFirstByNameIgnoreCase(String name);
}
