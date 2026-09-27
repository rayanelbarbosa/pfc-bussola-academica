import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../models/auth.model';

const TOKEN_KEY = 'bussola.token';
const USER_KEY = 'bussola.user';

/**
 * Sessão do usuário no front-end. O JWT fica no localStorage do navegador (sem cookies)
 * e é descartado no logout ou quando expira.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  private readonly currentUser = signal<User | null>(this.restoreUser());

  readonly user = this.currentUser.asReadonly();
  readonly isLoggedIn = computed(() => this.currentUser() !== null);
  readonly isAdmin = computed(() => this.currentUser()?.role === 'ADMIN');

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(tap((res) => this.store(res)));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, request).pipe(tap((res) => this.store(res)));
  }

  logout(sessionExpired = false): void {
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
