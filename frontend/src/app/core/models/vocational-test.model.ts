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
  /** Tipos do código, em ordem: o 1º é o tipo principal da carreira. */
  categories: { category: string; label: string }[];
  description: string;
  /** Até 3 tarefas do dia a dia (O*NET); vazio quando a carreira não tem. */
  dailyTasks: string[];
}

export interface VocationalTestResult {
  id: number;
  createdAt: string;
  scores: CategoryScore[];
  /** Pontuação máxima de cada tipo: 20 (5 atividades de 0 a 4); 10 nos resultados antigos, de 12 perguntas. */
  maxScore: number;
  topCategories: CategoryScore[];
  interestAreas: { value: string; label: string }[];
  /** As 4 carreiras que mais combinam entre as áreas escolhidas. */
  careerRecommendations: CareerRecommendation[];
  /**
   * Aviso de área: quando nenhuma carreira das áreas escolhidas tem o perfil como tipo principal, as 4 que mais
   * combinam com ele em todas as áreas. Vazia quando não há aviso.
   */
  suggestedCareers: CareerRecommendation[];
}

/** Escala do O*NET Interest Profiler, de 0 (detesto) a 4 (adoro), com emojis. */
export const LIKERT_OPTIONS: readonly { score: number; emoji: string; label: string }[] = [
  { score: 0, emoji: '😣', label: 'Detesto' },
  { score: 1, emoji: '🙁', label: 'Não gosto' },
  { score: 2, emoji: '😐', label: 'Não sei' },
  { score: 3, emoji: '🙂', label: 'Gosto' },
  { score: 4, emoji: '😍', label: 'Adoro' }
];

/** Atividades por tela: uma de cada tipo RIASEC (o Mini-IP vem em blocos de 6, na ordem R, I, A, S, E, C). */
export const QUESTIONS_PER_PAGE = 6;

/** Máximo de áreas de interesse por teste (mesma regra do back-end). */
export const MAX_INTEREST_AREAS = 3;
export const UNDECIDED_AREA = 'UNDECIDED';
