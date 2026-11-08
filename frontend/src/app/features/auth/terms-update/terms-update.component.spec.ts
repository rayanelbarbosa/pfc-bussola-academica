import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';

import { TermsUpdateComponent } from './terms-update.component';
import { environment } from '../../../../environments/environment';
import { authResponse } from '../../../core/testing/auth-fixtures';

describe('TermsUpdateComponent', () => {
  let fixture: ComponentFixture<TermsUpdateComponent>;
  let http: HttpTestingController;
  let router: Router;

  const terms = { version: '1.1', summary: 'Novo prazo de guarda', publishedAt: '2026-11-08T10:00:00', publishedBy: null };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [TermsUpdateComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
    fixture = TestBed.createComponent(TermsUpdateComponent);
    fixture.detectChanges();
    http.expectOne(`${environment.apiUrl}/terms/current`).flush(terms);
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('shows the version and the summary of the change', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Versão 1.1');
    expect(text).toContain('Novo prazo de guarda');
  });

  it('only accepts after the checkbox is marked', () => {
    const component = fixture.componentInstance;
    component.accept();
    http.expectNone(`${environment.apiUrl}/terms/accept`);

    component.accepted = true;
    component.accept();
    const req = http.expectOne(`${environment.apiUrl}/terms/accept`);
    expect(req.request.body).toEqual({ version: '1.1' });
    req.flush(authResponse());

    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });
});
