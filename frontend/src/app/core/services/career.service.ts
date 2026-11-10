import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Option } from '../models/admission-method.model';
import { Page } from '../models/audit.model';
import { Career, CareerRequest } from '../models/career.model';

export interface CareerFilter {
  name?: string;
  area?: string;
  active?: string;
  page: number;
  size: number;
}

/** Gestão das carreiras pelo administrador. */
@Injectable({ providedIn: 'root' })
export class CareerService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/admin/careers`;

  areas(): Observable<Option[]> {
    return this.http.get<Option[]>(`${this.url}/areas`);
  }

  categories(): Observable<Option[]> {
    return this.http.get<Option[]>(`${this.url}/categories`);
  }

  search(filter: CareerFilter): Observable<Page<Career>> {
    let params = new HttpParams().set('page', filter.page).set('size', filter.size);
    if (filter.name?.trim()) params = params.set('name', filter.name.trim());
    if (filter.area) params = params.set('area', filter.area);
    if (filter.active) params = params.set('active', filter.active);
    return this.http.get<Page<Career>>(this.url, { params });
  }

  create(request: CareerRequest): Observable<Career> {
    return this.http.post<Career>(this.url, request);
  }

  update(id: number, request: CareerRequest): Observable<Career> {
    return this.http.put<Career>(`${this.url}/${id}`, request);
  }

  changeStatus(id: number, active: boolean): Observable<Career> {
    return this.http.patch<Career>(`${this.url}/${id}/status`, { active });
  }
}
