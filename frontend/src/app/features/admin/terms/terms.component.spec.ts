import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';

import { TermsComponent, nextVersion } from './terms.component';
import { environment } from '../../../../environments/environment';

describe('TermsComponent (admin)', () => {
  let fixture: ComponentFixture<TermsComponent>;
  let http: HttpTestingController;
  let router: Router;

  const current = {
    version: '1.0',
    summary: 'Versão inicial',
    termsOfUse: '## 1. Termo atual',
    privacyPolicy: '## 1. Política atual',
    publishedAt: '2026-09-27T00:00:00',
    publishedBy: null
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TermsComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    fixture = TestBed.createComponent(TermsComponent);
    fixture.detectChanges();
    http.expectOne(`${environment.apiUrl}/admin/terms`).flush([current]);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('starts from the current texts and suggests the next version', () => {
    const form = fixture.componentInstance.form.getRawValue();
    expect(form.version).toBe('1.1');
    expect(form.termsOfUse).toBe('## 1. Termo atual');
    expect(form.privacyPolicy).toBe('## 1. Política atual');
  });

  it('publishes the edited texts and sends the admin to accept them', () => {
    const component = fixture.componentInstance;
    component.form.patchValue({ summary: 'Novo prazo', termsOfUse: '## 1. Termo novo' });

    component.askConfirmation();
    component.publish();

    const req = http.expectOne(`${environment.apiUrl}/admin/terms`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      version: '1.1',
      summary: 'Novo prazo',
      termsOfUse: '## 1. Termo novo',
      privacyPolicy: '## 1. Política atual'
    });
    req.flush({ ...current, version: '1.1' });
    expect(router.navigate).toHaveBeenCalledWith(['/novo-aceite'], { queryParams: { voltar: '/admin/termos' } });
  });

  it('does not publish without a summary', () => {
    fixture.componentInstance.askConfirmation();
    expect(fixture.componentInstance.confirming()).toBeFalse();
  });

  it('suggests the next version number', () => {
    expect(nextVersion('1.0')).toBe('1.1');
    expect(nextVersion('2')).toBe('2.1');
    expect(nextVersion('1.9')).toBe('1.10');
  });
});
