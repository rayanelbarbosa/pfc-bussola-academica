package br.com.bussolaacademica.testevocacional;

import java.util.List;

import static br.com.bussolaacademica.testevocacional.CategoriaRiasec.*;

/**
 * Banco fixo de perguntas do questionario vocacional (2 perguntas por
 * categoria RIASEC, 12 no total). Em uma versao futura este banco pode vir
 * do banco de dados via um painel administrativo; para a primeira regra de
 * negocio completa, as perguntas sao fixas e o que e persistido e o
 * resultado calculado de cada usuario.
 */
public final class BancoDePerguntas {

    public static final List<Pergunta> PERGUNTAS = List.of(
            new Pergunta(1, "Gosto de montar, consertar ou construir coisas com as mãos.", REALISTA),
            new Pergunta(2, "Prefiro atividades práticas a atividades só teóricas.", REALISTA),
            new Pergunta(3, "Gosto de investigar como as coisas funcionam e resolver problemas complexos.", INVESTIGATIVO),
            new Pergunta(4, "Tenho curiosidade científica e gosto de analisar dados e experimentos.", INVESTIGATIVO),
            new Pergunta(5, "Gosto de atividades criativas, como desenhar, escrever ou compor.", ARTISTICO),
            new Pergunta(6, "Prefiro ambientes com liberdade para me expressar de forma original.", ARTISTICO),
            new Pergunta(7, "Gosto de ajudar, ensinar ou cuidar de outras pessoas.", SOCIAL),
            new Pergunta(8, "Me sinto bem trabalhando em equipe e ouvindo os problemas dos outros.", SOCIAL),
            new Pergunta(9, "Gosto de liderar projetos e convencer pessoas sobre uma ideia.", EMPREENDEDOR),
            new Pergunta(10, "Tenho interesse em negócios, vendas ou empreender algo próprio.", EMPREENDEDOR),
            new Pergunta(11, "Gosto de organizar informações, planilhas e seguir procedimentos claros.", CONVENCIONAL),
            new Pergunta(12, "Prefiro tarefas com regras bem definidas a tarefas muito abertas.", CONVENCIONAL)
    );

    private BancoDePerguntas() {
    }
}
