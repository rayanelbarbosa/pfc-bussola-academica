import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { authInterceptor } from './auth.interceptor';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';
import { authResponse } from '../testing/auth-fixtures';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting(), provideRouter([])]
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => localStorage.clear());

  function loginAsStudent(): void {
    auth.login({ email: 'a@b.com', password: 'x' }).subscribe();
    controller.expectOne(`${environment.apiUrl}/auth/login`).flush(authResponse());
  }

  it('sends the JWT to the API', () => {
    loginAsStudent();
    http.get(`${environment.apiUrl}/users/me`).subscribe();

    const req = controller.expectOne(`${environment.apiUrl}/users/me`);
    expect(req.request.headers.get('Authorization')).toMatch(/^Bearer /);
    req.flush({});
  });

  it('does not send the token to other hosts', () => {
    loginAsStudent();
    http.get('https://example.com/qualquer').subscribe();

    const req = controller.expectOne('https://example.com/qualquer');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('logs out when the API answers 401', () => {
    loginAsStudent();
    spyOn(auth, 'logout');
    http.get(`${environment.apiUrl}/users/me`).subscribe({ error: () => undefined });

    controller.expectOne(`${environment.apiUrl}/users/me`).flush({}, { status: 401, statusText: 'Unauthorized' });
    expect(auth.logout).toHaveBeenCalledWith(true);
  });
});
