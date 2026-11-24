import { Injectable, computed, signal } from '@angular/core';

import { MAX_INTEREST_AREAS, UNDECIDED_AREA } from '../models/vocational-test.model';

/** v2: questionário do Mini-IP (30 atividades, notas de 0 a 4). Rascunhos da versão antiga são descartados. */
const STORAGE_KEY = 'bussola.quiz-draft.v2';
const OLD_STORAGE_KEY = 'bussola.quiz-draft';

export interface QuizDraft {
  areas: string[];
  /** questionId -> nota (0 a 4) */
  answers: Record<number, number>;
  /** Tela atual do questionário (6 atividades por tela). */
  current: number;
  /** Id do usuário dono do rascunho: o rascunho de uma conta nunca aparece para outra. */
  owner?: number;
}

const EMPTY: QuizDraft = { areas: [], answers: {}, current: 0 };

/**
 * Rascunho do teste vocacional (áreas escolhidas, respostas e pergunta atual), salvo no navegador
 * para a pessoa não perder o progresso se sair e voltar. O rascunho pertence a um usuário: é apagado
 * quando a sessão termina e descartado se outra conta entrar no mesmo navegador.
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

  /**
   * Liga o rascunho ao usuário da sessão. Sem usuário, apaga; com outro usuário (ou um rascunho
   * antigo sem dono), começa do zero. O mesmo usuário continua de onde parou.
   */
  bindToUser(userId: number | null): void {
    if (userId === null) {
      this.clear();
      return;
    }
    if (this.state().owner !== userId) {
      this.clear();
      this.update({ owner: userId });
    }
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
      localStorage.removeItem(OLD_STORAGE_KEY);
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return { ...EMPTY };
      const parsed = JSON.parse(raw) as Partial<QuizDraft>;
      return {
        areas: Array.isArray(parsed.areas) ? parsed.areas : [],
        answers: parsed.answers && typeof parsed.answers === 'object' ? parsed.answers : {},
        current: typeof parsed.current === 'number' ? parsed.current : 0,
        owner: typeof parsed.owner === 'number' ? parsed.owner : undefined
      };
    } catch {
      return { ...EMPTY };
    }
  }
}
