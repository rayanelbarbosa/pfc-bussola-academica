/** Informações de exibição de cada categoria RIASEC (cores do protótipo e descrição curta). */
export interface CategoryInfo {
  color: string;
  description: string;
}

const CATEGORIES: Record<string, CategoryInfo> = {
  REALISTIC: {
    color: '#2f9e45',
    description: 'você gosta de atividades práticas e de ver resultados concretos do seu trabalho.'
  },
  INVESTIGATIVE: {
    color: '#1d6fa5',
    description: 'a curiosidade te move e você gosta de compreender problemas em profundidade.'
  },
  ARTISTIC: {
    color: '#8b5cf6',
    description: 'você valoriza criatividade, originalidade e liberdade para se expressar.'
  },
  SOCIAL: {
    color: '#e8598b',
    description: 'você se realiza ajudando, ensinando e convivendo com outras pessoas.'
  },
  ENTERPRISING: {
    color: '#ff7a45',
    description: 'você gosta de liderar, tomar iniciativa e convencer pessoas sobre suas ideias.'
  },
  CONVENTIONAL: {
    color: '#5c6bc0',
    description: 'você se sente bem com organização, método e procedimentos bem definidos.'
  }
};

const DEFAULT_INFO: CategoryInfo = { color: '#123b57', description: '' };

export function getCategoryInfo(category: string): CategoryInfo {
  return CATEGORIES[category] ?? DEFAULT_INFO;
}
