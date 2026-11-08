package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.Career;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRepository extends JpaRepository<Career, Integer> {
}
