import { Injectable, computed, signal } from '@angular/core';

import { MAX_INTEREST_AREAS, UNDECIDED_AREA } from '../models/vocational-test.model';

const STORAGE_KEY = 'bussola.quiz-draft';

export interface QuizDraft {
  areas: string[];
  /** questionId -> nota (1 a 5) */
  answers: Record<number, number>;
  current: number;
}

const EMPTY: QuizDraft = { areas: [], answers: {}, current: 0 };

/**
 * Rascunho do teste vocacional (áreas escolhidas, respostas e pergunta atual), salvo no navegador
 * para a pessoa não perder o progresso se sair e voltar.
 */
@Injectable({ providedIn: 'root' })
export class QuizDraftService {
  private readonly state = signal<QuizDraft>(this.restore());

  readonly draft = this.state.asReadonly();
  readonly areas = computed(() => this.state().areas);
  readonly answeredCount = computed(() => Object.keys(this.state().answers).length);

  /** Seleciona/desmarca uma área. Devolve uma mensagem quando a regra impede a seleção. */
  toggleArea(area: string): string | null {
    const selected = this.state().areas;
    let next: string[];
    if (selected.includes(area)) {
      next = selected.filter((item) => item !== area);
    } else if (area === UNDECIDED_AREA) {
      next = [UNDECIDED_AREA];
    } else {
      const withoutUndecided = selected.filter((item) => item !== UNDECIDED_AREA);
      if (withoutUndecided.length >= MAX_INTEREST_AREAS) {
        return `Você pode escolher no máximo ${MAX_INTEREST_AREAS} áreas.`;
      }
      next = [...withoutUndecided, area];
    }
    this.update({ areas: next });
    return null;
  }

  answer(questionId: number, score: number): void {
    this.update({ answers: { ...this.state().answers, [questionId]: score } });
  }

  goTo(index: number): void {
    this.update({ current: Math.max(0, index) });
  }

  clear(): void {
    this.state.set({ ...EMPTY });
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      /* armazenamento indisponível */
    }
  }

  private update(patch: Partial<QuizDraft>): void {
    const next = { ...this.state(), ...patch };
    this.state.set(next);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
    } catch {
      /* armazenamento indisponível */
    }
  }

  private restore(): QuizDraft {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return { ...EMPTY };
      const parsed = JSON.parse(raw) as Partial<QuizDraft>;
      return {
        areas: Array.isArray(parsed.areas) ? parsed.areas : [],
        answers: parsed.answers && typeof parsed.answers === 'object' ? parsed.answers : {},
        current: typeof parsed.current === 'number' ? parsed.current : 0
      };
    } catch {
      return { ...EMPTY };
    }
  }
}
