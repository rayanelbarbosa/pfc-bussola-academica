import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Video } from '../models/video.model';

/** Vídeos de apoio por área (o back-end consulta a YouTube Data API v3). */
@Injectable({ providedIn: 'root' })
export class VideoService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/videos`;

  searchByArea(area: string): Observable<Video[]> {
    return this.http.get<Video[]>(this.baseUrl, { params: new HttpParams().set('area', area) });
  }
}
