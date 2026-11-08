import { TestBed } from '@angular/core/testing';

import { QuizDraftService } from './quiz-draft.service';

describe('QuizDraftService', () => {
  let service: QuizDraftService;

  beforeEach(() => {
    localStorage.removeItem('bussola.quiz-draft');
    TestBed.configureTestingModule({});
    service = TestBed.inject(QuizDraftService);
  });

  afterEach(() => localStorage.removeItem('bussola.quiz-draft'));

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
    service.answer(1, 5);
    service.goTo(1);

    const restored = TestBed.runInInjectionContext(() => new QuizDraftService());
    expect(restored.draft().answers).toEqual({ 1: 5 });
    expect(restored.draft().current).toBe(1);

    service.clear();
    expect(service.answeredCount()).toBe(0);
    expect(localStorage.getItem('bussola.quiz-draft')).toBeNull();
  });
});
