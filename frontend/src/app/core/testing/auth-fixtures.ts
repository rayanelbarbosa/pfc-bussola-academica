import { AuthResponse } from '../models/auth.model';

/** Gera um JWT falso (só o payload importa para o front). */
export function fakeToken(expiresInSeconds: number, role = 'STUDENT'): string {
  const payload = btoa(JSON.stringify({ sub: '1', role, exp: Math.floor(Date.now() / 1000) + expiresInSeconds }));
  return `header.${payload}.assinatura`;
}

export function authResponse(role: 'STUDENT' | 'ADMIN' = 'STUDENT'): AuthResponse {
  return {
    token: fakeToken(3600, role),
    tokenType: 'Bearer',
    expiresInSeconds: 3600,
    user: {
      id: 1,
      name: 'Marina Alves',
      email: 'marina@teste.com',
      role,
      termsAcceptedAt: '2026-09-27T10:00:00',
      termsVersion: '1.0',
      createdAt: '2026-09-27T10:00:00'
    }
  };
}
