import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { Career } from '../../../core/models/career.model';
import { CareerService } from '../../../core/services/career.service';
import { CareersComponent } from './careers.component';

const MEDICINA: Career = {
  id: 1, name: 'Medicina', area: 'HEALTH', areaLabel: 'Saúde', primaryCategory: 'INVESTIGATIVE',
  primaryCategoryLabel: 'Investigativo', secondaryCategory: 'SOCIAL', secondaryCategoryLabel: 'Social',
  tertiaryCategory: 'REALISTIC', tertiaryCategoryLabel: 'Realista', code: 'ISR', onetCode: '29-1216.00',
  description: 'Cuida da saúde.', dailyTasks: ['Atende pacientes', 'Pede exames'], active: true, updatedAt: '2026-11-10T10:00:00'
};

describe('CareersComponent', () => {
  let fixture: ComponentFixture<CareersComponent>;
  let service: jasmine.SpyObj<CareerService>;

  beforeEach(async () => {
    service = jasmine.createSpyObj('CareerService', ['areas', 'categories', 'search', 'create', 'update', 'changeStatus']);
    service.areas.and.returnValue(of([{ value: 'HEALTH', label: 'Saúde' }]));
    service.categories.and.returnValue(of([{ value: 'INVESTIGATIVE', label: 'Investigativo' }, { value: 'SOCIAL', label: 'Social' }]));
    service.search.and.returnValue(of({ content: [MEDICINA], page: 0, size: 10, totalElements: 1, totalPages: 1 }));
    await TestBed.configureTestingModule({
      imports: [CareersComponent],
      providers: [provideRouter([]), { provide: CareerService, useValue: service }]
    }).compileComponents();
    fixture = TestBed.createComponent(CareersComponent);
    fixture.detectChanges();
  });

  it('lists the careers with the RIASEC code', () => {
    const table = fixture.nativeElement.querySelector('tbody').textContent;
    expect(table).toContain('Medicina');
    expect(table).toContain('Saúde');
    expect(table).toContain('O*NET 29-1216.00');
    expect(table).toContain('I');
  });

  it('blocks the same type as primary and secondary', () => {
    const component = fixture.componentInstance;
    component.openCreate();
    component.form.patchValue({ name: 'Nova', area: 'HEALTH', primaryCategory: 'SOCIAL', secondaryCategory: 'SOCIAL', description: 'D' });
    component.form.controls.secondaryCategory.markAsTouched();
    component.save();
    expect(service.create).not.toHaveBeenCalled();
    expect(component.sameType).toBeTrue();
  });

  it('blocks a third type equal to one of the first two and previews the three-letter code', () => {
    const component = fixture.componentInstance;
    component.openCreate();
    component.form.patchValue({ name: 'Nova', area: 'HEALTH', primaryCategory: 'INVESTIGATIVE', secondaryCategory: 'SOCIAL', tertiaryCategory: 'SOCIAL', description: 'D' });
    component.save();
    expect(component.sameTertiary).toBeTrue();
    expect(service.create).not.toHaveBeenCalled();

    component.form.patchValue({ tertiaryCategory: 'REALISTIC' });
    fixture.detectChanges();
    expect(component.codePreview()).toBe('ISR');
    expect(fixture.nativeElement.textContent).toContain('sem perfil O*NET, o maior escore R');
  });

  it('creates a career and shows the RIASEC code preview', () => {
    service.create.and.returnValue(of({ ...MEDICINA, id: 50, name: 'Engenharia de Prompt' }));
    const component = fixture.componentInstance;
    component.openCreate();
    component.form.patchValue({ name: 'Engenharia de Prompt', area: 'HEALTH', primaryCategory: 'INVESTIGATIVE', secondaryCategory: 'SOCIAL', description: 'D',
      dailyTasks: ' Escreve prompts \n\nTesta modelos' });
    fixture.detectChanges();
    expect(component.codePreview()).toBe('IS');
    expect(fixture.nativeElement.textContent).toContain('Código RIASEC');
    component.save();
    expect(service.create).toHaveBeenCalledWith(jasmine.objectContaining({ name: 'Engenharia de Prompt', active: true, tertiaryCategory: null,
      dailyTasks: ['Escreve prompts', 'Testa modelos'] }));
    expect(component.notice()).toContain('Engenharia de Prompt');
    expect(component.editingId()).toBeNull();
  });

  it('edits and shows the API error', () => {
    service.update.and.returnValue(throwError(() => new HttpErrorResponse({ status: 409, error: { message: 'Já existe uma carreira com esse nome nessa área' } })));
    const component = fixture.componentInstance;
    component.openEdit(MEDICINA);
    expect(component.form.controls.name.value).toBe('Medicina');
    expect(component.form.controls.tertiaryCategory.value).toBe('REALISTIC');
    expect(component.form.controls.dailyTasks.value).toBe('Atende pacientes\nPede exames');
    component.save();
    expect(service.update).toHaveBeenCalledWith(1, jasmine.objectContaining({ primaryCategory: 'INVESTIGATIVE' }));
    expect(component.formError()).toContain('Já existe');
  });

  it('rejects more than 3 daily tasks or the | separator', () => {
    const control = fixture.componentInstance.form.controls.dailyTasks;
    control.setValue('a\nb\nc\nd');
    expect(control.hasError('tasks')).toBeTrue();
    control.setValue('a|b');
    expect(control.hasError('tasks')).toBeTrue();
    control.setValue('a\nb\nc');
    expect(control.valid).toBeTrue();
  });

  it('deactivates after confirmation', () => {
    service.changeStatus.and.returnValue(of({ ...MEDICINA, active: false }));
    const component = fixture.componentInstance;
    component.confirming.set(MEDICINA);
    component.confirmStatusChange();
    expect(service.changeStatus).toHaveBeenCalledWith(1, false);
    expect(component.notice()).toContain('desativada');
  });

  it('filters and clears the filters', () => {
    const component = fixture.componentInstance;
    component.filterArea = 'HEALTH';
    component.load(0);
    expect(service.search).toHaveBeenCalledWith(jasmine.objectContaining({ area: 'HEALTH' }));
    component.clearFilters();
    expect(component.filterArea).toBe('');
  });

  it('shows an error when the list fails', () => {
    service.search.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    fixture.componentInstance.load(0);
    expect(fixture.componentInstance.error()).toBeTruthy();
  });
});
