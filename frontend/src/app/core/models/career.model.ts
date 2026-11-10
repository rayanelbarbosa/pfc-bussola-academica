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
  description: string;
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
  active: boolean;
}
