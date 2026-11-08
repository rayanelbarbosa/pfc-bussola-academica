import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { AppComponent } from './app.component';
import { authResponse } from './core/testing/auth-fixtures';

describe('AppComponent', () => {
  afterEach(() => localStorage.clear());

  async function render() {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideRouter([]), provideHttpClient()]
    }).compileComponents();
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('renders the header for visitors and the footer with the legal links', async () => {
    const el = await render();

    expect(el.querySelector('router-outlet')).toBeTruthy();
    expect(el.querySelector('header a[href="/login"]')).toBeTruthy();
    expect(el.querySelector('a[href="/termos-de-uso"]')).toBeTruthy();
    expect(el.querySelector('a[href="/politica-de-privacidade"]')).toBeTruthy();
    expect(el.textContent).not.toContain('Projeto Final');
  });

  it('identifies the administrator in the header', async () => {
    const admin = authResponse('ADMIN');
    localStorage.setItem('bussola.token', admin.token);
    localStorage.setItem('bussola.user', JSON.stringify(admin.user));

    const el = await render();

    expect(el.querySelector('header')?.textContent).toContain('Administração');
    expect(el.querySelector('header')?.textContent).toContain('Administrador');
  });
});
