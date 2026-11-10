import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AdmissionMethod } from '../../core/models/admission-method.model';
import { AdmissionMethodService } from '../../core/services/admission-method.service';
import { AdmissionGuideComponent } from './admission-guide.component';

function method(id: number, name: string, type: string, typeLabel: string): AdmissionMethod {
  return {
    id, name, type, typeLabel, summary: `Resumo ${name}`, description: 'O que é', howItWorks: ['Passo 1', 'Passo 2'],
    eligibility: 'Quem pode', schedule: 'Quando', cost: 'Gratuito', tips: null, officialUrl: 'https://gov.br',
    active: true, updatedAt: '2026-11-07T10:00:00'
  };
}

describe('AdmissionGuideComponent', () => {
  let fixture: ComponentFixture<AdmissionGuideComponent>;
  let service: jasmine.SpyObj<AdmissionMethodService>;

  async function setup(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [AdmissionGuideComponent],
      providers: [provideRouter([]), { provide: AdmissionMethodService, useValue: service }]
    }).compileComponents();
    fixture = TestBed.createComponent(AdmissionGuideComponent);
    fixture.detectChanges();
  }

  beforeEach(() => {
    service = jasmine.createSpyObj('AdmissionMethodService', ['listActive']);
  });

  it('lists the admission methods with their expandable content and official link', async () => {
    service.listActive.and.returnValue(of([method(1, 'Enem', 'NATIONAL_EXAM', 'Exame nacional')]));
    await setup();
    const el: HTMLElement = fixture.nativeElement;

    expect(el.querySelectorAll('details').length).toBe(1);
    expect(el.textContent).toContain('Enem');
    expect(el.querySelectorAll('details ol li').length).toBe(2);
    expect(el.querySelector('details a[href="https://gov.br"]')).toBeTruthy();
  });

  it('filters by type', async () => {
    service.listActive.and.returnValue(of([
      method(1, 'Enem', 'NATIONAL_EXAM', 'Exame nacional'),
      method(2, 'Fies', 'STUDENT_FINANCING', 'Financiamento estudantil')
    ]));
    await setup();

    fixture.componentInstance.selectedType.set('STUDENT_FINANCING');
    fixture.detectChanges();

    const names = Array.from(fixture.nativeElement.querySelectorAll('details h3')).map((h) => (h as HTMLElement).textContent);
    expect(names).toEqual(['Fies']);
  });

  it('shows an error with a retry button when the API fails', async () => {
    service.listActive.and.returnValue(throwError(() => new Error('falha')));
    await setup();

    expect(fixture.nativeElement.querySelector('[role="alert"]')).toBeTruthy();
  });

  it('picks an icon for each admission type', async () => {
    service.listActive.and.returnValue(of([]));
    await setup();
    expect(fixture.componentInstance.iconFor('STUDENT_FINANCING')).toBe('wallet');
    expect(fixture.componentInstance.iconFor('OUTRO')).toBe('book-open');
  });
});
