import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { AdmissionMethod } from '../../../core/models/admission-method.model';
import { AdmissionMethodService } from '../../../core/services/admission-method.service';
import { AdmissionMethodsComponent } from './admission-methods.component';

const ENEM: AdmissionMethod = {
  id: 1, name: 'Enem', type: 'NATIONAL_EXAM', typeLabel: 'Exame nacional', summary: 'Resumo', description: 'O que é',
  howItWorks: ['Passo 1', 'Passo 2'], eligibility: 'Quem', schedule: 'Quando', cost: 'Custo', tips: null,
  officialUrl: 'https://gov.br', active: true, updatedAt: '2026-11-07T10:00:00'
};

describe('AdmissionMethodsComponent', () => {
  let fixture: ComponentFixture<AdmissionMethodsComponent>;
  let service: jasmine.SpyObj<AdmissionMethodService>;

  beforeEach(async () => {
    service = jasmine.createSpyObj('AdmissionMethodService', ['types', 'search', 'create', 'update', 'changeStatus']);
    service.types.and.returnValue(of([{ value: 'NATIONAL_EXAM', label: 'Exame nacional' }]));
    service.search.and.returnValue(of({ content: [ENEM], page: 0, size: 10, totalElements: 1, totalPages: 1 }));
    await TestBed.configureTestingModule({
      imports: [AdmissionMethodsComponent],
      providers: [provideRouter([]), { provide: AdmissionMethodService, useValue: service }]
    }).compileComponents();
    fixture = TestBed.createComponent(AdmissionMethodsComponent);
    fixture.detectChanges();
  });

  it('lists the methods in a table', () => {
    expect(fixture.nativeElement.querySelector('tbody').textContent).toContain('Enem');
  });

  it('does not save an invalid form (empty fields and link without http)', () => {
    const component = fixture.componentInstance;
    component.openCreate();
    component.form.patchValue({ name: 'Novo', officialUrl: 'ftp://errado' });
    component.save();

    expect(service.create).not.toHaveBeenCalled();
    expect(component.form.controls.officialUrl.invalid).toBeTrue();
  });

  it('edits keeping the steps one per line', () => {
    const component = fixture.componentInstance;
    component.openEdit(ENEM);
    expect(component.form.controls.howItWorks.value).toBe('Passo 1\nPasso 2');

    service.update.and.returnValue(of(ENEM));
    component.save();
    expect(service.update).toHaveBeenCalledWith(1, jasmine.objectContaining({ name: 'Enem', tips: null }));
    expect(component.notice()).toContain('alterada');
  });

  it('deactivates after confirmation', () => {
    service.changeStatus.and.returnValue(of({ ...ENEM, active: false }));
    fixture.componentInstance.confirming.set(ENEM);
    fixture.componentInstance.confirmStatusChange();

    expect(service.changeStatus).toHaveBeenCalledWith(1, false);
  });
});
