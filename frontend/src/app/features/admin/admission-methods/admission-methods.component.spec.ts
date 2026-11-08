import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

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

  it('accepts an empty official link (optional field)', () => {
    const component = fixture.componentInstance;
    component.openCreate();
    component.form.patchValue({ officialUrl: '' });
    expect(component.form.controls.officialUrl.valid).toBeTrue();
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

  const fail = () => throwError(() => new HttpErrorResponse({ status: 500 }));

  it('creates a new method without official link and shows a notice', () => {
    const component = fixture.componentInstance;
    component.openEdit({ ...ENEM, officialUrl: null });
    component.editingId.set(0);
    service.create.and.returnValue(of({ ...ENEM, id: 2, name: 'Novo' }));

    component.save();

    expect(service.create).toHaveBeenCalledWith(jasmine.objectContaining({ officialUrl: '' }));
    expect(component.notice()).toContain('incluída');
    expect(component.editingId()).toBeNull();
  });

  it('keeps the form open with an error when saving fails', () => {
    const component = fixture.componentInstance;
    component.openEdit(ENEM);
    service.update.and.returnValue(fail());

    component.save();

    expect(component.formError()).toBeTruthy();
    expect(component.saving()).toBeFalse();
    expect(component.editingId()).toBe(1);
    component.closeForm();
    expect(component.editingId()).toBeNull();
  });

  it('shows an error when the list or the status change fails', () => {
    const component = fixture.componentInstance;
    service.search.and.returnValue(fail());
    component.load(0);
    expect(component.error()).toBeTruthy();
    expect(component.loading()).toBeFalse();

    service.changeStatus.and.returnValue(fail());
    component.confirming.set(ENEM);
    component.confirmStatusChange();
    expect(component.error()).toContain('status');
  });

  it('does nothing when there is no method to confirm', () => {
    fixture.componentInstance.confirming.set(null);
    fixture.componentInstance.confirmStatusChange();
    expect(service.changeStatus).not.toHaveBeenCalled();
  });

  it('clears the filters and reloads the first page', () => {
    const component = fixture.componentInstance;
    component.filterName = 'Enem';
    component.filterType = 'NATIONAL_EXAM';
    component.filterActive = 'true';
    service.search.calls.reset();

    component.clearFilters();

    expect(component.filterName).toBe('');
    expect(service.search).toHaveBeenCalledWith(jasmine.objectContaining({ name: '', type: '', active: '', page: 0 }));
  });

  it('counts the characters typed in a field', () => {
    const component = fixture.componentInstance;
    component.form.controls.summary.setValue('abc');
    expect(component.length('summary')).toBe(3);
  });
});
