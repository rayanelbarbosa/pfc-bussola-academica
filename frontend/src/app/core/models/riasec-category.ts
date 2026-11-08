/** Conteúdo de exibição de cada tipo RIASEC: letra, cor do tema e textos explicativos. */
export interface CategoryInfo {
  key: string;
  letter: string;
  name: string;
  short: string;
  meaning: string;
  traits: string;
  environments: string;
}

export const RIASEC_INFO: CategoryInfo[] = [
  {
    key: 'REALISTIC', letter: 'R', name: 'Realista', short: 'Prático, gosta de fazer com as mãos.',
    meaning: 'Interesse por atividades práticas, ferramentas, máquinas e resultados concretos.',
    traits: 'Praticidade, habilidade manual e preferência por aprender fazendo.',
    environments: 'Oficinas, laboratórios, obras, atividades de campo e espaços com tarefas concretas.'
  },
  {
    key: 'INVESTIGATIVE', letter: 'I', name: 'Investigativo', short: 'Curioso, analítico e investigador.',
    meaning: 'Interesse por investigar perguntas, compreender fenômenos e resolver problemas.',
    traits: 'Curiosidade, análise, observação e busca de evidências.',
    environments: 'Pesquisa, laboratórios, tecnologia e equipes que valorizam autonomia intelectual.'
  },
  {
    key: 'ARTISTIC', letter: 'A', name: 'Artístico', short: 'Criativo, expressivo e original.',
    meaning: 'Interesse por criar e expressar ideias por diferentes linguagens.',
    traits: 'Imaginação, originalidade e preferência por experimentar novas possibilidades.',
    environments: 'Estúdios, produção cultural, design e espaços com liberdade criativa.'
  },
  {
    key: 'SOCIAL', letter: 'S', name: 'Social', short: 'Gosta de ajudar, ensinar e cuidar.',
    meaning: 'Interesse por ajudar, ensinar, orientar e cuidar de pessoas.',
    traits: 'Escuta, cooperação, comunicação e atenção às necessidades dos outros.',
    environments: 'Escolas, serviços de saúde, projetos sociais e equipes com contato humano frequente.'
  },
  {
    key: 'ENTERPRISING', letter: 'E', name: 'Empreendedor', short: 'Líder, persuasivo e ousado.',
    meaning: 'Interesse por liderar, negociar, mobilizar pessoas e desenvolver iniciativas.',
    traits: 'Iniciativa, persuasão, disposição para tomar decisões e trabalhar com metas.',
    environments: 'Gestão, negócios, vendas e projetos que envolvem liderança e negociação.'
  },
  {
    key: 'CONVENTIONAL', letter: 'C', name: 'Convencional', short: 'Organizado, preciso e metódico.',
    meaning: 'Interesse por organizar dados, seguir procedimentos e manter processos confiáveis.',
    traits: 'Atenção a detalhes, planejamento, precisão e organização.',
    environments: 'Administração, finanças e atividades com informações estruturadas e regras claras.'
  }
];

export function getCategoryInfo(category: string): CategoryInfo | undefined {
  return RIASEC_INFO.find((info) => info.key === category || info.letter === category);
}

/** Classe CSS da cor do tipo (ex.: "riasec-r"). */
export function riasecClass(category: string): string {
  const info = getCategoryInfo(category);
  return info ? `riasec-${info.letter.toLowerCase()}` : '';
}
