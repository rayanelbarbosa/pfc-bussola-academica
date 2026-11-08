import { ChangeDetectionStrategy, Component, HostListener, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { LIKERT_OPTIONS, Question } from '../../../core/models/vocational-test.model';
import { getErrorMessage } from '../../../core/services/api-error';
import { QuizDraftService } from '../../../core/services/quiz-draft.service';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';

/** Tempo até avançar sozinho para a próxima afirmação depois de responder. */
export const AUTO_ADVANCE_MS = 300;

/**
 * Etapa 2 do teste (RF06): uma afirmação por vez, com avanço automático ao responder,
 * atalhos de teclado (1 a 5) e progresso salvo no navegador.
 */
@Component({
  selector: 'app-questions',
  standalone: true,
  imports: [IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './questions.component.html'
})
export class QuestionsComponent implements OnInit, OnDestroy {
  private readonly testService = inject(VocationalTestService);
  private readonly draft = inject(QuizDraftService);
  private readonly router = inject(Router);
  private timer: ReturnType<typeof setTimeout> | null = null;

  readonly options = LIKERT_OPTIONS;
  readonly questions = signal<Question[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly submitting = signal(false);

  readonly index = computed(() => Math.min(this.draft.draft().current, Math.max(this.questions().length - 1, 0)));
  readonly question = computed(() => this.questions()[this.index()]);
  readonly answers = computed(() => this.draft.draft().answers);
  readonly isLast = computed(() => this.index() === this.questions().length - 1);
  readonly allAnswered = computed(
    () => this.questions().length > 0 && this.questions().every((q) => this.answers()[q.id] !== undefined)
  );

  ngOnInit(): void {
    if (!this.draft.areas().length) {
      this.router.navigate(['/teste-vocacional'], { replaceUrl: true });
      return;
    }
    this.testService.getQuestions().subscribe({
      next: (questions) => {
        this.questions.set(questions);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível carregar as perguntas.'));
        this.loading.set(false);
      }
    });
  }

  ngOnDestroy(): void {
    this.cancelAdvance();
  }

  respond(score: number): void {
    const question = this.question();
    if (!question || this.timer !== null || this.submitting()) {
      return;
    }
    this.draft.answer(question.id, score);
    if (this.isLast()) {
      return;
    }
    this.timer = setTimeout(() => {
      this.timer = null;
      this.draft.goTo(this.index() + 1);
    }, AUTO_ADVANCE_MS);
  }

  back(): void {
    this.cancelAdvance();
    if (this.index() === 0) {
      this.router.navigate(['/teste-vocacional']);
    } else {
      this.draft.goTo(this.index() - 1);
    }
  }

  @HostListener('window:keydown', ['$event'])
  onKey(event: KeyboardEvent): void {
    const target = event.target as HTMLElement | null;
    if (event.repeat || event.altKey || event.ctrlKey || event.metaKey ||
      (target && ['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName))) {
      return;
    }
    if (/^[1-5]$/.test(event.key)) {
      event.preventDefault();
      this.respond(Number(event.key));
    }
  }

  submit(): void {
    if (!this.allAnswered() || this.submitting()) {
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    const answers = this.questions().map((q) => ({ questionId: q.id, score: this.answers()[q.id] }));
    this.testService.submitAnswers({ answers, interestAreas: this.draft.areas() }).subscribe({
      next: (result) => {
        this.draft.clear();
        this.router.navigate(['/teste-vocacional/resultado', result.id]);
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível enviar suas respostas. Tente novamente.'));
        this.submitting.set(false);
      }
    });
  }

  private cancelAdvance(): void {
    if (this.timer !== null) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  }
}
