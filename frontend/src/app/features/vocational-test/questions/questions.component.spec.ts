import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { Question, VocationalTestResult } from '../../../core/models/vocational-test.model';
import { QuizDraftService } from '../../../core/services/quiz-draft.service';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { QuestionsComponent } from './questions.component';

const TYPES = ['REALISTIC', 'INVESTIGATIVE', 'ARTISTIC', 'SOCIAL', 'ENTERPRISING', 'CONVENTIONAL'];

/** 12 atividades = 2 telas de 6, na ordem do Mini-IP (R, I, A, S, E, C). */
const QUESTIONS: Question[] = Array.from({ length: 12 }, (_, i) => ({
  id: i + 1,
  statement: 'Atividade ' + (i + 1),
  category: TYPES[i % 6]
}));

describe('QuestionsComponent', () => {
  let fixture: ComponentFixture<QuestionsComponent>;
  let testService: jasmine.SpyObj<VocationalTestService>;
  let draft: QuizDraftService;
  let router: Router;

  beforeEach(async () => {
    localStorage.removeItem('bussola.quiz-draft.v2');
    testService = jasmine.createSpyObj('VocationalTestService', ['getQuestions', 'submitAnswers']);
    testService.getQuestions.and.returnValue(of(QUESTIONS));
    await TestBed.configureTestingModule({
      imports: [QuestionsComponent],
      providers: [provideRouter([]), { provide: VocationalTestService, useValue: testService }]
    }).compileComponents();
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    spyOn(window, 'scrollTo');
    draft = TestBed.inject(QuizDraftService);
  });

  afterEach(() => draft.clear());

  function start(): QuestionsComponent {
    draft.toggleArea('HEALTH');
    fixture = TestBed.createComponent(QuestionsComponent);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  function answerPage(component: QuestionsComponent, score: number): void {
    component.pageQuestions().forEach((question) => component.respond(question.id, score));
  }

  it('sends back to the area step when no area was chosen', () => {
    fixture = TestBed.createComponent(QuestionsComponent);
    fixture.detectChanges();
    expect(router.navigate).toHaveBeenCalledWith(['/teste-vocacional'], { replaceUrl: true });
  });

  it('shows 6 activities per page, one of each RIASEC type, with the 5 emoji options', () => {
    const component = start();
    const el: HTMLElement = fixture.nativeElement;

    expect(component.pageCount()).toBe(2);
    expect(component.pageQuestions().map((q) => q.category)).toEqual(TYPES);
    expect(el.querySelectorAll('fieldset').length).toBe(6);
    expect(el.querySelectorAll('fieldset')[0].querySelectorAll('button').length).toBe(5);
    expect(el.textContent).toContain('Detesto');
    expect(el.textContent).toContain('Adoro');
    expect(el.textContent).toContain('USDOL/ETA has not approved');
  });

  it('only goes to the next page after answering the 6 activities of the current one', () => {
    const component = start();

    component.next();
    expect(component.page()).toBe(0);

    answerPage(component, 3);
    expect(component.answeredCount()).toBe(6);
    component.next();
    expect(component.page()).toBe(1);
    expect(component.pageQuestions()[0].id).toBe(7);
    expect(window.scrollTo).toHaveBeenCalled();
  });

  it('submits the answers (0 to 4) with the chosen areas on the last page', () => {
    const component = start();
    answerPage(component, 0);
    component.next();
    answerPage(component, 4);
    expect(component.isLast()).toBeTrue();
    expect(component.allAnswered()).toBeTrue();

    window.dataLayer = [];
    testService.submitAnswers.and.returnValue(of({
      id: 9,
      topCategories: [{ category: 'SOCIAL', label: 'Social', score: 18 }, { category: 'ARTISTIC', label: 'Artístico', score: 18 }]
    } as unknown as VocationalTestResult));
    component.submit();

    const sent = testService.submitAnswers.calls.mostRecent().args[0];
    expect(sent.interestAreas).toEqual(['HEALTH']);
    expect(sent.answers.length).toBe(12);
    expect(sent.answers[0]).toEqual({ questionId: 1, score: 0 });
    expect(sent.answers[11]).toEqual({ questionId: 12, score: 4 });
    expect(router.navigate).toHaveBeenCalledWith(['/teste-vocacional/resultado', 9]);
    expect(draft.answeredCount()).toBe(0);
    expect(window.dataLayer).toContain(jasmine.objectContaining({ event: 'test_completed', profile: 'SOCIAL,ARTISTIC', tie: true }));
  });

  it('marks the chosen option when clicked', () => {
    start();
    const el: HTMLElement = fixture.nativeElement;
    const adoro = Array.from(el.querySelectorAll('fieldset')[0].querySelectorAll('button'))
      .find((button) => button.getAttribute('aria-label') === 'Adoro')!;

    adoro.click();
    fixture.detectChanges();

    expect(draft.draft().answers[1]).toBe(4);
    expect(adoro.getAttribute('aria-pressed')).toBe('true');
  });

  it('shows an error when the questions cannot be loaded', () => {
    testService.getQuestions.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    const component = start();
    expect(component.error()).toBeTruthy();
    expect(component.loading()).toBeFalse();
  });

  it('goes back to the areas on the first page and to the previous page after that', () => {
    const component = start();
    component.back();
    expect(router.navigate).toHaveBeenCalledWith(['/teste-vocacional']);

    draft.goTo(1);
    component.back();
    expect(component.page()).toBe(0);
  });

  it('does not submit before all answers and shows an error when sending fails', () => {
    const component = start();
    component.submit();
    expect(testService.submitAnswers).not.toHaveBeenCalled();

    QUESTIONS.forEach((question) => draft.answer(question.id, 2));
    testService.submitAnswers.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    component.submit();
    expect(component.error()).toBeTruthy();
    expect(component.submitting()).toBeFalse();
  });
});
