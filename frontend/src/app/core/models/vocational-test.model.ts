/** Modelos do teste vocacional — espelham os DTOs do back-end (pacote dto). */

export interface Question {
  id: number;
  statement: string;
  category: string;
}

export interface Answer {
  questionId: number;
  score: number;
}

export interface SubmitTestRequest {
  answers: Answer[];
}

export interface CategoryScore {
  category: string;
  label: string;
  score: number;
}

export interface VocationalTestResult {
  id: number;
  createdAt: string;
  scores: CategoryScore[];
  topCategories: CategoryScore[];
  recommendedAreas: string[];
}

/** Escala Likert usada em todas as perguntas. */
export const LIKERT_OPTIONS: ReadonlyArray<{ score: number; label: string }> = [
  { score: 1, label: 'Discordo totalmente' },
  { score: 2, label: 'Discordo' },
  { score: 3, label: 'Neutro' },
  { score: 4, label: 'Concordo' },
  { score: 5, label: 'Concordo totalmente' }
];

/** Escore máximo por categoria: 2 perguntas x nota 5. */
export const MAX_SCORE = 10;
