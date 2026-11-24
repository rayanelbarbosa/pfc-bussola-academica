import { TestBed } from '@angular/core/testing';

import { QuizDraftService } from './quiz-draft.service';

describe('QuizDraftService', () => {
  let service: QuizDraftService;

  beforeEach(() => {
    localStorage.removeItem('bussola.quiz-draft.v2');
    TestBed.configureTestingModule({});
    service = TestBed.inject(QuizDraftService);
  });

  afterEach(() => localStorage.removeItem('bussola.quiz-draft.v2'));

  it('allows at most 3 interest areas', () => {
    expect(service.toggleArea('HEALTH')).toBeNull();
    expect(service.toggleArea('ENGINEERING')).toBeNull();
    expect(service.toggleArea('BUSINESS')).toBeNull();

    expect(service.toggleArea('EDUCATION')).toContain('no máximo 3');
    expect(service.areas()).toEqual(['HEALTH', 'ENGINEERING', 'BUSINESS']);
  });

  it('"Ainda não sei" clears the other areas and vice versa', () => {
    service.toggleArea('HEALTH');
    service.toggleArea('UNDECIDED');
    expect(service.areas()).toEqual(['UNDECIDED']);

    service.toggleArea('ARTS_AND_COMMUNICATION');
    expect(service.areas()).toEqual(['ARTS_AND_COMMUNICATION']);
  });

  it('unselects an area clicked twice', () => {
    service.toggleArea('HEALTH');
    service.toggleArea('HEALTH');
    expect(service.areas()).toEqual([]);
  });

  it('keeps the answers in the browser and clears them at the end', () => {
    service.toggleArea('HEALTH');
    service.answer(1, 4);
    service.goTo(1);

    const restored = TestBed.runInInjectionContext(() => new QuizDraftService());
    expect(restored.draft().answers).toEqual({ 1: 4 });
    expect(restored.draft().current).toBe(1);

    service.clear();
    expect(service.answeredCount()).toBe(0);
    expect(localStorage.getItem('bussola.quiz-draft.v2')).toBeNull();
  });

  it('discards a draft of the old questionnaire (12 statements, scores 1 to 5)', () => {
    localStorage.setItem('bussola.quiz-draft', JSON.stringify({ areas: ['HEALTH'], answers: { 1: 5 }, current: 3 }));

    const restored = TestBed.runInInjectionContext(() => new QuizDraftService());

    expect(restored.draft().answers).toEqual({});
    expect(localStorage.getItem('bussola.quiz-draft')).toBeNull();
  });
  it('keeps the draft of the same user and discards the draft of another account', () => {
    service.bindToUser(1);
    service.toggleArea('HEALTH');
    service.answer(1, 4);

    service.bindToUser(1);
    expect(service.draft().answers).toEqual({ 1: 4 });

    service.bindToUser(2);
    expect(service.areas()).toEqual([]);
    expect(service.answeredCount()).toBe(0);
    expect(service.draft().owner).toBe(2);
  });

  it('discards a draft without owner and clears everything when nobody is logged in', () => {
    localStorage.setItem('bussola.quiz-draft.v2', JSON.stringify({ areas: ['HEALTH'], answers: { 1: 4 }, current: 2 }));
    const restored = TestBed.runInInjectionContext(() => new QuizDraftService());

    restored.bindToUser(7);
    expect(restored.draft().answers).toEqual({});

    restored.answer(1, 3);
    restored.bindToUser(null);
    expect(restored.answeredCount()).toBe(0);
    expect(localStorage.getItem('bussola.quiz-draft.v2')).toBeNull();
  });
});
