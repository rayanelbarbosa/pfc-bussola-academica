import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { QuizDraftService } from '../../../core/services/quiz-draft.service';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { AreasComponent } from './areas.component';

describe('AreasComponent', () => {
  let fixture: ComponentFixture<AreasComponent>;
  let router: Router;

  beforeEach(async () => {
    localStorage.removeItem('bussola.quiz-draft');
    const testService = jasmine.createSpyObj('VocationalTestService', ['getInterestAreas']);
    testService.getInterestAreas.and.returnValue(of([
      { value: 'HEALTH', label: 'Saúde' }, { value: 'ENGINEERING', label: 'Engenharias' },
      { value: 'BUSINESS', label: 'Negócios e Gestão' }, { value: 'EDUCATION', label: 'Educação' }
    ]));
    await TestBed.configureTestingModule({
      imports: [AreasComponent],
      providers: [provideRouter([]), { provide: VocationalTestService, useValue: testService }]
    }).compileComponents();
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    fixture = TestBed.createComponent(AreasComponent);
    fixture.detectChanges();
  });

  afterEach(() => TestBed.inject(QuizDraftService).clear());

  it('only starts after choosing at least one area', () => {
    const start: HTMLButtonElement = fixture.nativeElement.querySelector('button.btn-primary');
    expect(start.disabled).toBeTrue();

    fixture.componentInstance.toggle('HEALTH');
    fixture.detectChanges();
    expect(start.disabled).toBeFalse();

    fixture.componentInstance.start();
    expect(router.navigate).toHaveBeenCalledWith(['/teste-vocacional/perguntas']);
  });

  it('warns when trying to choose a 4th area', () => {
    ['HEALTH', 'ENGINEERING', 'BUSINESS', 'EDUCATION'].forEach((area) => fixture.componentInstance.toggle(area));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('no máximo 3');
    expect(fixture.nativeElement.textContent).toContain('3 de 3 selecionadas');
  });
});
