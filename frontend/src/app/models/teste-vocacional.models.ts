/**
 * Modelos do teste vocacional (regra de negocio: pontuacao RIASEC).
 * Espelham os DTOs do back-end (pacote br.com.bussolaacademica.testevocacional).
 */

export interface Pergunta {
  id: number;
  enunciado: string;
  categoria: string;
}

export interface RespostaDTO {
  perguntaId: number;
  nota: number;
}

export interface SubmeterTesteRequest {
  respostas: RespostaDTO[];
}

export interface ResultadoTesteResponse {
  id: number;
  escoresPorCategoria: Record<string, number>;
  categoriasPrincipais: string[];
  areasRecomendadas: string[];
}
