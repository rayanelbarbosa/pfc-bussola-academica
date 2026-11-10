import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Video } from '../models/video.model';

/** Vídeos de apoio de uma carreira cadastrada (o back-end consulta a YouTube Data API v3). */
@Injectable({ providedIn: 'root' })
export class VideoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/videos`;

  /**
   * {@code profile} = tipos RIASEC principais do estudante (ex.: ['SOCIAL', 'INVESTIGATIVE']). O back-end usa o
   * perfil só para ordenar os vídeos (ranking); a busca no YouTube continua a mesma e fica em cache.
   */
  searchByCareer(career: string, profile: string[] = []): Observable<Video[]> {
    let params = new HttpParams().set('career', career);
    if (profile.length) params = params.set('profile', profile.join(','));
    return this.http.get<Video[]>(this.baseUrl, { params });
  }
}
