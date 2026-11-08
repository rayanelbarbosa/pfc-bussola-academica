import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { VideoService } from './video.service';

describe('VideoService', () => {
  it('searches videos by the registered career name', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const http = TestBed.inject(HttpTestingController);
    TestBed.inject(VideoService).searchByCareer('Medicina').subscribe();
    const req = http.expectOne((r) => r.url === `${environment.apiUrl}/videos`);
    expect(req.request.params.get('career')).toBe('Medicina');
    req.flush([]);
    http.verify();
  });
});
