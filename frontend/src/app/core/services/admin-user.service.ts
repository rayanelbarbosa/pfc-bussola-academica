import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Page } from '../models/audit.model';
import { ResultSummary, Role } from '../models/auth.model';

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  role: Role;
  createdAt: string;
}

/** Cadastro e histórico de testes de um usuário, visto pelo administrador (consulta auditada). */
export interface AdminUserDetail extends AdminUser {
  termsVersion: string;
  termsAcceptedAt: string;
  results: ResultSummary[];
}

export interface UserFilter {
  search?: string;
  role?: string;
  page: number;
  size: number;
}

/** Gestão de usuários e administradores (RF12). */
@Injectable({ providedIn: 'root' })
export class AdminUserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/users`;

  search(filter: UserFilter): Observable<Page<AdminUser>> {
    let params = new HttpParams().set('page', filter.page).set('size', filter.size);
    if (filter.search?.trim()) params = params.set('search', filter.search.trim());
    if (filter.role) params = params.set('role', filter.role);
    return this.http.get<Page<AdminUser>>(this.baseUrl, { params });
  }

  details(id: number): Observable<AdminUserDetail> {
    return this.http.get<AdminUserDetail>(`${this.baseUrl}/${id}`);
  }

  createAdmin(request: { name: string; email: string; password: string }): Observable<AdminUser> {
    return this.http.post<AdminUser>(this.baseUrl, request);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  changeRole(id: number, role: Role): Observable<AdminUser> {
    return this.http.patch<AdminUser>(`${this.baseUrl}/${id}/role`, { role });
  }
}
