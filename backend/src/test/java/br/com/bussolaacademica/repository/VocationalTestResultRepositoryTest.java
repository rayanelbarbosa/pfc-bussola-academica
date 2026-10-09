package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.model.VocationalTestResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de persistência com o banco H2 de teste (mesmas migrations do Flyway da produção).
 * Cada teste roda em uma transação desfeita ao final, então não depende de dados prévios.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VocationalTestResultRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VocationalTestResultRepository resultRepository;

    @Autowired
    private UserRepository userRepository;

    private User persistUser(String email) {
        return entityManager.persist(new User("Aluna", email, "hash", Role.STUDENT,
                LocalDateTime.of(2026, 9, 1, 10, 0), "1.0"));
    }

    private VocationalTestResult persistResult(User user, LocalDateTime createdAt) {
        Map<RiasecCategory, Integer> scores = new EnumMap<>(RiasecCategory.class);
        for (RiasecCategory category : RiasecCategory.values()) {
            scores.put(category, category == RiasecCategory.SOCIAL ? 10 : 4);
        }
        return entityManager.persist(new VocationalTestResult(user, createdAt, scores,
                List.of(RiasecCategory.SOCIAL), RiasecCategory.SOCIAL.getRecommendedAreas()));
    }

    @Test
    void deveListarResultadosDoUsuarioDoMaisRecenteParaOMaisAntigo() {
        // Arrange
        User aluna = persistUser("aluna-repo@teste.com");
        User outra = persistUser("outra-repo@teste.com");
        VocationalTestResult antigo = persistResult(aluna, LocalDateTime.of(2026, 9, 10, 8, 0));
        VocationalTestResult recente = persistResult(aluna, LocalDateTime.of(2026, 10, 1, 8, 0));
        persistResult(outra, LocalDateTime.of(2026, 10, 5, 8, 0));
        entityManager.flush();
        entityManager.clear();

        // Act
        List<VocationalTestResult> historico = resultRepository.findByUser_IdOrderByCreatedAtDesc(aluna.getId());

        // Assert
        assertEquals(2, historico.size());
        assertEquals(recente.getId(), historico.get(0).getId());
        assertEquals(antigo.getId(), historico.get(1).getId());
        assertEquals(List.of("Pedagogia", "Psicologia", "Enfermagem"), historico.get(0).getRecommendedAreas());
    }

    @Test
    void deveExcluirSomenteOsResultadosDoUsuarioInformado() {
        // Arrange
        User aluna = persistUser("aluna-del@teste.com");
        User outra = persistUser("outra-del@teste.com");
        persistResult(aluna, LocalDateTime.of(2026, 9, 10, 8, 0));
        persistResult(outra, LocalDateTime.of(2026, 9, 11, 8, 0));
        entityManager.flush();

        // Act
        resultRepository.deleteByUser_Id(aluna.getId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertTrue(resultRepository.findByUser_IdOrderByCreatedAtDesc(aluna.getId()).isEmpty());
        assertEquals(1, resultRepository.findByUser_IdOrderByCreatedAtDesc(outra.getId()).size());
    }

    @Test
    void deveEncontrarUsuarioPeloEmailIgnorandoMaiusculas() {
        // Arrange
        persistUser("caixa-mista@teste.com");
        entityManager.flush();

        // Act + Assert
        assertTrue(userRepository.findByEmailIgnoreCase("CAIXA-Mista@TESTE.com").isPresent());
        assertTrue(userRepository.existsByEmailIgnoreCase("Caixa-Mista@teste.com"));
    }
}
