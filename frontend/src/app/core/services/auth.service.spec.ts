import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';

import { AuthService, isExpired } from './auth.service';
import { environment } from '../../../environments/environment';
import { authResponse, fakeToken } from '../testing/auth-fixtures';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])] });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('stores the session after login', () => {
    service.login({ email: 'marina@teste.com', password: 'Senha1234' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(authResponse('ADMIN'));

    expect(service.isLoggedIn()).toBeTrue();
    expect(service.isAdmin()).toBeTrue();
    expect(service.token()).toContain('header.');
  });

  it('stores the session after registration', () => {
    service
      .register({ name: 'Marina', email: 'marina@teste.com', password: 'Senha1234', ageConfirmed: true, acceptedTerms: true })
      .subscribe();
    const req = http.expectOne(`${environment.apiUrl}/auth/register`);
    expect(req.request.body.acceptedTerms).toBeTrue();
    expect(req.request.body.ageConfirmed).toBeTrue();
    req.flush(authResponse());

    expect(service.user()?.name).toBe('Marina Alves');
    expect(service.isAdmin()).toBeFalse();
  });

  it('revokes the token on the server, clears the session and goes to login on logout', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    service.login({ email: 'a@b.com', password: 'x' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(authResponse());

    service.logout();
    http.expectOne(`${environment.apiUrl}/auth/logout`).flush(null, { status: 204, statusText: 'No Content' });

    expect(service.isLoggedIn()).toBeFalse();
    expect(service.token()).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {});
  });

  it('does not call the API when the session already expired', () => {
    spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
    service.login({ email: 'a@b.com', password: 'x' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(authResponse());

    service.logout(true);

    http.expectNone(`${environment.apiUrl}/auth/logout`);
    expect(service.isLoggedIn()).toBeFalse();
  });

  it('keeps the pending terms returned by the login and clears them after accepting', () => {
    const pendingTerms = { version: '1.1', summary: 'Mudou', publishedAt: '2026-11-08T10:00:00', publishedBy: null };
    service.login({ email: 'a@b.com', password: 'x' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush({ ...authResponse(), pendingTerms });
    expect(service.pendingTerms()?.version).toBe('1.1');

    service.acceptTerms('1.1').subscribe();
    const req = http.expectOne(`${environment.apiUrl}/terms/accept`);
    expect(req.request.body).toEqual({ version: '1.1' });
    req.flush(authResponse());

    expect(service.pendingTerms()).toBeNull();
  });

  it('detects expired or unreadable tokens', () => {
    expect(isExpired(fakeToken(-10))).toBeTrue();
    expect(isExpired(fakeToken(600))).toBeFalse();
    expect(isExpired('lixo')).toBeTrue();
  });
});
