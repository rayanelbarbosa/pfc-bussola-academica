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
  description: string;
  active: boolean;
}
