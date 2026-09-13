package br.com.bussolaacademica.testevocacional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TesteVocacionalServiceTest {

    @Autowired
    private TesteVocacionalService service;

    @Autowired
    private ResultadoTesteRepository repository;

    @Test
    void deveListarAsDozePerguntasDoBanco() {
        List<Pergunta> perguntas = service.listarPerguntas();

        assertThat(perguntas).hasSize(12);
    }

    @Test
    void deveCalcularCategoriaRealistaComoPrincipalQuandoRespostasFavorecemEssaCategoria() {
        // Perguntas 1 e 2 sao da categoria Realista; demais perguntas com nota minima.
        SubmeterTesteRequest request = new SubmeterTesteRequest(List.of(
                new RespostaDTO(1, 5),
                new RespostaDTO(2, 5),
                new RespostaDTO(3, 1),
                new RespostaDTO(4, 1),
                new RespostaDTO(5, 1),
                new RespostaDTO(6, 1),
                new RespostaDTO(7, 1),
                new RespostaDTO(8, 1),
                new RespostaDTO(9, 1),
                new RespostaDTO(10, 1),
                new RespostaDTO(11, 1),
                new RespostaDTO(12, 1)
        ));

        ResultadoTesteResponse resultado = service.calcularEPersistirResultado(request);

        assertThat(resultado.escoresPorCategoria().get("Realista")).isEqualTo(10);
        assertThat(resultado.categoriasPrincipais()).containsExactly("Realista");
        assertThat(resultado.areasRecomendadas())
                .contains("Engenharias", "Tecnologia da Informação", "Agronomia");
        assertThat(resultado.id()).isNotNull();
    }

    @Test
    void devePersistirOResultadoNoBancoDeDados() {
        SubmeterTesteRequest request = new SubmeterTesteRequest(List.of(
                new RespostaDTO(7, 5),
                new RespostaDTO(8, 5)
        ));

        long totalAntes = repository.count();

        ResultadoTesteResponse resultado = service.calcularEPersistirResultado(request);

        assertThat(repository.count()).isEqualTo(totalAntes + 1);
        assertThat(repository.findById(resultado.id())).isPresent();
        assertThat(repository.findById(resultado.id()).get().getEscoreSocial()).isEqualTo(10);
    }

    @Test
    void deveEmpatarEntreDuasCategoriasQuandoOsEscoresForemIguais() {
        SubmeterTesteRequest request = new SubmeterTesteRequest(List.of(
                new RespostaDTO(1, 5),
                new RespostaDTO(2, 5),
                new RespostaDTO(9, 5),
                new RespostaDTO(10, 5)
        ));

        ResultadoTesteResponse resultado = service.calcularEPersistirResultado(request);

        assertThat(resultado.categoriasPrincipais())
                .containsExactlyInAnyOrder("Realista", "Empreendedor");
    }

    @Test
    void deveLancarExcecaoQuandoPerguntaNaoExiste() {
        SubmeterTesteRequest request = new SubmeterTesteRequest(List.of(
                new RespostaDTO(999, 3)
        ));

        org.junit.jupiter.api.Assertions.assertThrows(
                PerguntaInvalidaException.class,
                () -> service.calcularEPersistirResultado(request)
        );
    }
}
