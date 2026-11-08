/** Modelos de autenticação — espelham os DTOs do back-end. */

export type Role = 'STUDENT' | 'ADMIN';

export interface User {
  id: number;
  name: string;
  email: string;
  role: Role;
  termsAcceptedAt: string;
  termsVersion: string;
  createdAt: string;
}

/** Versão do Termo de Uso e da Política de Privacidade publicada pelo administrador. */
export interface TermsVersion {
  version: string;
  summary: string;
  /** Textos completos, no formato de app-legal-document (## título, - lista, | tabela |, **negrito**, [link](url)). */
  termsOfUse: string;
  privacyPolicy: string;
  publishedAt: string;
  publishedBy: string | null;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresInSeconds: number;
  user: User;
  /** Preenchido quando existe uma versão nova dos termos que o usuário ainda não aceitou. */
  pendingTerms?: TermsVersion | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  ageConfirmed: boolean;
  acceptedTerms: boolean;
}

export interface ResultSummary {
  id: number;
  createdAt: string;
  topCategories: string[];
  interestAreas: string[];
}
