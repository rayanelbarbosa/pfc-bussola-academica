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

export interface AuthResponse {
  token: string;
  tokenType: string;
  expiresInSeconds: number;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
  acceptedTerms: boolean;
}

export interface ResultSummary {
  id: number;
  createdAt: string;
  topCategories: string[];
}

/** Versão vigente do Termo de Uso e da Política de Privacidade. */
export const LEGAL_VERSION = '1.0';
export const LEGAL_EFFECTIVE_DATE = '27/09/2026';
