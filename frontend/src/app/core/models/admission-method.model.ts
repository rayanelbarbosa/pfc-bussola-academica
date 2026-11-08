/** Forma de ingresso no ensino superior (espelha AdmissionMethodResponse do back-end). */
export interface AdmissionMethod {
  id: number;
  name: string;
  type: string;
  typeLabel: string;
  summary: string;
  description: string;
  howItWorks: string[];
  eligibility: string;
  schedule: string;
  cost: string;
  tips: string | null;
  officialUrl: string;
  active: boolean;
  updatedAt: string;
}

/** Dados enviados pelo administrador; "howItWorks" vai como texto, um passo por linha. */
export interface AdmissionMethodRequest {
  name: string;
  type: string;
  summary: string;
  description: string;
  howItWorks: string;
  eligibility: string;
  schedule: string;
  cost: string;
  tips: string | null;
  officialUrl: string;
  active: boolean;
}

export interface Option {
  value: string;
  label: string;
}
