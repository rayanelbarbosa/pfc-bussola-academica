import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, TermsVersion, User } from '../models/auth.model';

const TOKEN_KEY = 'bussola.token';
const USER_KEY = 'bussola.user';

/**
 * Sessão do usuário no front-end. O JWT fica no localStorage do navegador (sem cookies).
 * No logout o token é revogado também no servidor (RF05), não só apagado do navegador.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  private readonly currentUser = signal<User | null>(this.restoreUser());
  private readonly pending = signal<TermsVersion | null>(null);

  readonly user = this.currentUser.asReadonly();
  readonly isLoggedIn = computed(() => this.currentUser() !== null);
  readonly isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');
  /** Versão nova dos termos ainda não aceita (vem no login). */
  readonly pendingTerms = this.pending.asReadonly();

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(tap((res) => this.store(res)));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, request).pipe(tap((res) => this.store(res)));
  }

  /** Versão vigente do Termo/Política (rota pública). */
  currentTerms(): Observable<TermsVersion> {
    return this.http.get<TermsVersion>(`${environment.apiUrl}/terms/current`);
  }

  /** Novo aceite: a API devolve um token novo, já liberado. */
  acceptTerms(version: string): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/terms/accept`, { version })
      .pipe(tap((res) => this.store(res)));
  }

  /**
   * Sai da conta. Se a sessão ainda vale, avisa a API para revogar o token (o pedido sai antes de o
   * token ser apagado daqui); a tela não espera a resposta.
   */
  logout(sessionExpired = false): void {
    if (!sessionExpired && this.token()) {
      this.http.post<void>(`${this.baseUrl}/logout`, {}).subscribe({ error: () => undefined });
    }
    this.clear();
    this.router.navigate(['/login'], sessionExpired ? { queryParams: { expirou: 1 } } : {});
  }

  /** Limpa a sessão sem navegar (ex.: após excluir a conta). */
  clear(): void {
    safeStorage(() => {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    });
    this.currentUser.set(null);
    this.pending.set(null);
  }

  token(): string | null {
    const token = safeStorage(() => localStorage.getItem(TOKEN_KEY)) ?? null;
    if (token && isExpired(token)) {
      this.clear();
      return null;
    }
    return token;
  }

  private store(response: AuthResponse): void {
    safeStorage(() => {
      localStorage.setItem(TOKEN_KEY, response.token);
      localStorage.setItem(USER_KEY, JSON.stringify(response.user));
    });
    this.currentUser.set(response.user);
    this.pending.set(response.pendingTerms ?? null);
  }

  private restoreUser(): User | null {
    const token = safeStorage(() => localStorage.getItem(TOKEN_KEY));
    const raw = safeStorage(() => localStorage.getItem(USER_KEY));
    if (!token || !raw || isExpired(token)) {
      return null;
    }
    try {
      return JSON.parse(raw) as User;
    } catch {
      return null;
    }
  }
}

/** Lê a data de expiração (claim "exp") do JWT. Token ilegível é tratado como expirado. */
export function isExpired(token: string): boolean {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const { exp } = JSON.parse(atob(payload)) as { exp?: number };
    return !exp || exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

function safeStorage<T>(operation: () => T): T | undefined {
  try {
    return operation();
  } catch {
    return undefined;
  }
}
