import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { Question, VocationalTestResult } from '../../../core/models/vocational-test.model';
import { QuizDraftService } from '../../../core/services/quiz-draft.service';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { AUTO_ADVANCE_MS, QuestionsComponent } from './questions.component';

const QUESTIONS: Question[] = [
  { id: 1, statement: 'Pergunta 1', category: 'REALISTIC' },
  { id: 2, statement: 'Pergunta 2', category: 'SOCIAL' }
];

describe('QuestionsComponent', () => {
  let fixture: ComponentFixture<QuestionsComponent>;
  let testService: jasmine.SpyObj<VocationalTestService>;
  let draft: QuizDraftService;
  let router: Router;

  beforeEach(async () => {
    localStorage.removeItem('bussola.quiz-draft');
    testService = jasmine.createSpyObj('VocationalTestService', ['getQuestions', 'submitAnswers']);
    testService.getQuestions.and.returnValue(of(QUESTIONS));
    await TestBed.configureTestingModule({
      imports: [QuestionsComponent],
      providers: [provideRouter([]), { provide: VocationalTestService, useValue: testService }]
    }).compileComponents();
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    draft = TestBed.inject(QuizDraftService);
  });

  afterEach(() => draft.clear());

  it('sends back to the area step when no area was chosen', () => {
    fixture = TestBed.createComponent(QuestionsComponent);
    fixture.detectChanges();
    expect(router.navigate).toHaveBeenCalledWith(['/teste-vocacional'], { replaceUrl: true });
  });

  it('advances automatically after answering and submits with the chosen areas', fakeAsync(() => {
    draft.toggleArea('HEALTH');
    fixture = TestBed.createComponent(QuestionsComponent);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    component.respond(4);
    tick(AUTO_ADVANCE_MS);
    expect(component.index()).toBe(1);

    component.respond(2);
    expect(component.isLast()).toBeTrue();
    expect(component.allAnswered()).toBeTrue();

    testService.submitAnswers.and.returnValue(of({ id: 9 } as unknown as VocationalTestResult));
    component.submit();

    expect(testService.submitAnswers).toHaveBeenCalledWith({
      answers: [{ questionId: 1, score: 4 }, { questionId: 2, score: 2 }],
      interestAreas: ['HEALTH']
    });
    expect(router.navigate).toHaveBeenCalledWith(['/teste-vocacional/resultado', 9]);
    expect(draft.answeredCount()).toBe(0);
  }));

  it('answers with the keyboard (keys 1 to 5)', () => {
    draft.toggleArea('HEALTH');
    fixture = TestBed.createComponent(QuestionsComponent);
    fixture.detectChanges();

    window.dispatchEvent(new KeyboardEvent('keydown', { key: '5' }));
    expect(draft.draft().answers[1]).toBe(5);
  });
});
