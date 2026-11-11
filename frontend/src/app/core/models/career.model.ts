/** Carreira na gestão do administrador (código RIASEC de 2 letras: tipo principal + secundário). */
export interface Career {
  id: number;
  name: string;
  area: string;
  areaLabel: string;
  primaryCategory: string;
  primaryCategoryLabel: string;
  secondaryCategory: string;
  secondaryCategoryLabel: string;
  /** 3º tipo do código de Holland (opcional): só desempata carreiras com a mesma compatibilidade. */
  tertiaryCategory: string | null;
  tertiaryCategoryLabel: string | null;
  code: string;
  /** Ocupação do O*NET usada como referência do código e do perfil (ex.: "29-1216.00"); null se cadastrada sem. */
  onetCode: string | null;
  description: string;
  /** Até 3 tarefas do dia a dia (O*NET), mostradas no "Mais sobre a carreira". */
  dailyTasks: string[];
  active: boolean;
  updatedAt: string;
}

export interface CareerRequest {
  name: string;
  area: string;
  primaryCategory: string;
  secondaryCategory: string;
  tertiaryCategory: string | null;
  description: string;
  /** Opcional: até 3 tarefas do dia a dia. */
  dailyTasks?: string[];
  active: boolean;
}
