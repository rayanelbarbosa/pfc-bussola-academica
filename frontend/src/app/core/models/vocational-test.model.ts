/** Modelos do teste vocacional: espelham os DTOs do back-end (pacote dto). */

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
  interestAreas: string[];
}

export interface CategoryScore {
  category: string;
  label: string;
  score: number;
}

export interface CareerRecommendation {
  name: string;
  area: string;
  areaLabel: string;
  code: string;
  categories: { category: string; label: string }[];
  compatibility: number;
  description: string;
  /** Carreira seguinte do ranking que empatou com esta; nulo sem empate. */
  tiedWith: string | null;
  /** Critério que deixou esta na frente: perfil completo do O*NET, 3º tipo ou nome. */
  tieReason: 'PROFILE' | 'THIRD_TYPE' | 'NAME' | null;
}

/** Melhor carreira de cada área escolhida: mostra com qual área o perfil combina mais. */
export interface AreaMatch {
  area: string;
  label: string;
  bestCareer: string;
  bestCompatibility: number;
  /** Os 2 tipos da melhor carreira: explicam por que a área combina (ou não) com o perfil. */
  bestCategories: { category: string; label: string }[];
}

export interface VocationalTestResult {
  id: number;
  createdAt: string;
  scores: CategoryScore[];
  topCategories: CategoryScore[];
  interestAreas: { value: string; label: string }[];
  /** Ranking único: as 4 carreiras mais compatíveis entre as áreas escolhidas. */
  careerRecommendations: CareerRecommendation[];
  /** Áreas na ordem de compatibilidade (a primeira é a que mais combina). */
  areaMatches: AreaMatch[];
}

/** Escala Likert usada em todas as perguntas. */
export const LIKERT_OPTIONS: ReadonlyArray<{ score: number; label: string }> = [
  { score: 1, label: 'Discordo totalmente' },
  { score: 2, label: 'Discordo' },
  { score: 3, label: 'Neutro' },
  { score: 4, label: 'Concordo' },
  { score: 5, label: 'Concordo totalmente' }
];

/** Escore mínimo e máximo por categoria: 2 perguntas com nota de 1 a 5. */
export const MIN_SCORE = 2;
export const MAX_SCORE = 10;

/** Máximo de áreas de interesse por teste (mesma regra do back-end). */
export const MAX_INTEREST_AREAS = 3;
export const UNDECIDED_AREA = 'UNDECIDED';
