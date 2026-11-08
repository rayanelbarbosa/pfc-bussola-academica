import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Page } from '../models/audit.model';
import { AdmissionMethod, AdmissionMethodRequest, Option } from '../models/admission-method.model';

export interface AdmissionMethodFilter {
  name?: string;
  type?: string;
  active?: string;
  page: number;
  size: number;
}

/** Formas de ingresso: consulta pública (RF01) e gestão pelo administrador (RF11). */
@Injectable({ providedIn: 'root' })
export class AdmissionMethodService {
  private readonly http = inject(HttpClient);
  private readonly publicUrl = `${environment.apiUrl}/admission-methods`;
  private readonly adminUrl = `${environment.apiUrl}/admin/admission-methods`;

  listActive(): Observable<AdmissionMethod[]> {
    return this.http.get<AdmissionMethod[]>(this.publicUrl);
  }

  types(): Observable<Option[]> {
    return this.http.get<Option[]>(`${this.publicUrl}/types`);
  }

  search(filter: AdmissionMethodFilter): Observable<Page<AdmissionMethod>> {
    let params = new HttpParams().set('page', filter.page).set('size', filter.size);
    if (filter.name?.trim()) params = params.set('name', filter.name.trim());
    if (filter.type) params = params.set('type', filter.type);
    if (filter.active) params = params.set('active', filter.active);
    return this.http.get<Page<AdmissionMethod>>(this.adminUrl, { params });
  }

  create(request: AdmissionMethodRequest): Observable<AdmissionMethod> {
    return this.http.post<AdmissionMethod>(this.adminUrl, request);
  }

  update(id: number, request: AdmissionMethodRequest): Observable<AdmissionMethod> {
    return this.http.put<AdmissionMethod>(`${this.adminUrl}/${id}`, request);
  }

  changeStatus(id: number, active: boolean): Observable<AdmissionMethod> {
    return this.http.patch<AdmissionMethod>(`${this.adminUrl}/${id}/status`, { active });
  }
}
