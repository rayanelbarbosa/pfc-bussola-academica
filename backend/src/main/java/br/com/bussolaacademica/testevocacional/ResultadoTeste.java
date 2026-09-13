package br.com.bussolaacademica.testevocacional;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Registro persistido do resultado de uma aplicacao do teste vocacional.
 * Cada linha representa uma regra de negocio (pontuacao RIASEC) ja
 * calculada e salva no banco de dados.
 */
@Entity
@Table(name = "resultado_teste_vocacional")
public class ResultadoTeste {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "escore_realista", nullable = false)
    private int escoreRealista;

    @Column(name = "escore_investigativo", nullable = false)
    private int escoreInvestigativo;

    @Column(name = "escore_artistico", nullable = false)
    private int escoreArtistico;

    @Column(name = "escore_social", nullable = false)
    private int escoreSocial;

    @Column(name = "escore_empreendedor", nullable = false)
    private int escoreEmpreendedor;

    @Column(name = "escore_convencional", nullable = false)
    private int escoreConvencional;

    @Column(name = "categorias_principais", nullable = false, length = 200)
    private String categoriasPrincipais;

    @Column(name = "areas_recomendadas", nullable = false, length = 500)
    private String areasRecomendadas;

    protected ResultadoTeste() {
        // exigido pelo JPA
    }

    public ResultadoTeste(LocalDateTime dataHora, int escoreRealista, int escoreInvestigativo,
                          int escoreArtistico, int escoreSocial, int escoreEmpreendedor,
                          int escoreConvencional, String categoriasPrincipais, String areasRecomendadas) {
        this.dataHora = dataHora;
        this.escoreRealista = escoreRealista;
        this.escoreInvestigativo = escoreInvestigativo;
        this.escoreArtistico = escoreArtistico;
        this.escoreSocial = escoreSocial;
        this.escoreEmpreendedor = escoreEmpreendedor;
        this.escoreConvencional = escoreConvencional;
        this.categoriasPrincipais = categoriasPrincipais;
        this.areasRecomendadas = areasRecomendadas;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public int getEscoreRealista() {
        return escoreRealista;
    }

    public int getEscoreInvestigativo() {
        return escoreInvestigativo;
    }

    public int getEscoreArtistico() {
        return escoreArtistico;
    }

    public int getEscoreSocial() {
        return escoreSocial;
    }

    public int getEscoreEmpreendedor() {
        return escoreEmpreendedor;
    }

    public int getEscoreConvencional() {
        return escoreConvencional;
    }

    public String getCategoriasPrincipais() {
        return categoriasPrincipais;
    }

    public String getAreasRecomendadas() {
        return areasRecomendadas;
    }
}
