import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { HeaderComponent } from './header.component';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';
import { authResponse } from '../../../core/testing/auth-fixtures';

describe('HeaderComponent', () => {
  let fixture: ComponentFixture<HeaderComponent>;
  let auth: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => localStorage.clear());

  function loginAs(role: 'STUDENT' | 'ADMIN'): void {
    auth.login({ email: 'marina@teste.com', password: 'x' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(authResponse(role));
  }

  function render(): HTMLElement {
    fixture = TestBed.createComponent(HeaderComponent);
    fixture.detectChanges();
    return fixture.nativeElement;
  }

  it('shows the login link to visitors', () => {
    const el = render();
    expect(fixture.componentInstance.isLoggedIn()).toBeFalse();
    expect(fixture.componentInstance.firstName()).toBe('Conta');
    expect(el.textContent).toContain('Entrar');
  });

  it('shows the first name and opens/closes the admin menu', () => {
    loginAs('ADMIN');
    render();
    const header = fixture.componentInstance;
    expect(header.firstName()).toBe('Marina');
    expect(header.initial()).toBe('M');
    expect(header.isAdmin()).toBeTrue();

    header.toggleMenu('admin', new Event('click'));
    expect(header.openMenu()).toBe('admin');
    header.toggleMenu('admin', new Event('click'));
    expect(header.openMenu()).toBeNull();

    header.toggleMenu('user', new Event('click'));
    document.dispatchEvent(new Event('click'));
    expect(header.openMenu()).toBeNull();
  });

  it('closes the mobile menu and logs out', () => {
    loginAs('STUDENT');
    render();
    spyOn(auth, 'logout');
    fixture.componentInstance.mobileOpen.set(true);

    fixture.componentInstance.logout();

    expect(fixture.componentInstance.mobileOpen()).toBeFalse();
    expect(auth.logout).toHaveBeenCalled();
  });
});
