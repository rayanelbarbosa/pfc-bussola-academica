package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    List<Question> findAllByOrderByDisplayOrderAsc();
}
