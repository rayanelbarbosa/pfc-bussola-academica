import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { QuestionnaireComponent } from './questionnaire.component';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { Question } from '../../../core/models/vocational-test.model';

describe('QuestionnaireComponent', () => {
  const questions: Question[] = [
    { id: 1, statement: 'Gosto de construir coisas.', category: 'REALISTIC' },
    { id: 2, statement: 'Gosto de investigar.', category: 'INVESTIGATIVE' }
  ];
  let fixture: ComponentFixture<QuestionnaireComponent>;
  let component: QuestionnaireComponent;
  let service: jasmine.SpyObj<VocationalTestService>;
  let router: Router;

  beforeEach(async () => {
    service = jasmine.createSpyObj('VocationalTestService', ['getQuestions', 'submitAnswers']);
    service.getQuestions.and.returnValue(of(questions));

    await TestBed.configureTestingModule({
      imports: [QuestionnaireComponent],
      providers: [provideRouter([]), { provide: VocationalTestService, useValue: service }]
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    fixture = TestBed.createComponent(QuestionnaireComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const nextButton = (): HTMLButtonElement => fixture.nativeElement.querySelector('.btn-primary');

  it('shows the first question and the progress', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.statement')?.textContent).toContain('Gosto de construir coisas.');
    expect(el.querySelector('.counter')?.textContent).toContain('Pergunta 1 de 2');
    expect(el.querySelectorAll('.option').length).toBe(5);
  });

  it('enables "Próxima" only after an option is chosen', () => {
    expect(nextButton().disabled).toBeTrue();

    (fixture.nativeElement.querySelectorAll('.option')[3] as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(nextButton().disabled).toBeFalse();
    expect(fixture.nativeElement.querySelector('.option.selected')?.textContent).toContain('Concordo');
  });

  it('keeps the answer when going back to the previous question', () => {
    component.select(4);
    component.next();
    component.previous();

    expect(component.currentIndex()).toBe(0);
    expect(component.currentScore()).toBe(4);
  });

  it('submits all answers on the last question and opens the result', () => {
    service.submitAnswers.and.returnValue(
      of({ id: 10, createdAt: '', scores: [], topCategories: [], recommendedAreas: [] })
    );
    component.select(5);
    component.next();
    component.select(2);
    component.next();

    expect(service.submitAnswers).toHaveBeenCalledWith({
      answers: [
        { questionId: 1, score: 5 },
        { questionId: 2, score: 2 }
      ]
    });
    expect(router.navigate).toHaveBeenCalledWith(['/teste-vocacional/resultado', 10]);
  });

  it('shows the API error message when submission fails', () => {
    service.submitAnswers.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 400, error: { message: 'Responda todas as perguntas.' } }))
    );
    component.select(3);
    component.next();
    component.select(3);
    component.next();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.error-message')?.textContent).toContain('Responda todas as perguntas.');
    expect(component.submitting()).toBeFalse();
  });
});
