import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { adminGuard, authGuard, guestGuard } from './auth.guards';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';
import { authResponse } from '../testing/auth-fixtures';

describe('route guards', () => {
  const route = {} as ActivatedRouteSnapshot;
  const state = { url: '/teste-vocacional' } as RouterStateSnapshot;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()] });
  });

  afterEach(() => localStorage.clear());

  function loginAs(role: 'STUDENT' | 'ADMIN'): void {
    TestBed.inject(AuthService).login({ email: 'a@b.com', password: 'x' }).subscribe();
    TestBed.inject(HttpTestingController).expectOne(`${environment.apiUrl}/auth/login`).flush(authResponse(role));
  }

  const run = (guard: typeof authGuard) => TestBed.runInInjectionContext(() => guard(route, state));
  const urlOf = (result: unknown) => TestBed.inject(Router).serializeUrl(result as UrlTree);

  it('sends anonymous users to login keeping the requested page', () => {
    expect(urlOf(run(authGuard))).toBe('/login?voltar=%2Fteste-vocacional');
  });

  it('lets logged users in', () => {
    loginAs('STUDENT');
    expect(run(authGuard)).toBeTrue();
  });

  it('blocks students from the admin area', () => {
    loginAs('STUDENT');
    expect(urlOf(run(adminGuard))).toBe('/');
  });

  it('lets admins into the admin area', () => {
    loginAs('ADMIN');
    expect(run(adminGuard)).toBeTrue();
  });

  it('sends logged users away from the login page', () => {
    expect(run(guestGuard)).toBeTrue();
    loginAs('STUDENT');
    expect(urlOf(run(guestGuard))).toBe('/');
  });
});
