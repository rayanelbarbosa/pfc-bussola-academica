import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { LIKERT_OPTIONS, QUESTIONS_PER_PAGE, Question } from '../../../core/models/vocational-test.model';
import { getErrorMessage } from '../../../core/services/api-error';
import { QuizDraftService } from '../../../core/services/quiz-draft.service';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { AnalyticsService } from '../../../core/services/analytics.service';

/**
 * Etapa 2 do teste (RF06): as 30 atividades do O*NET Mini Interest Profiler em 5 telas de 6 (uma de cada tipo
 * RIASEC), respostas de 0 (detesto) a 4 (adoro) com emojis, barra de progresso e rascunho salvo no navegador.
 */
@Component({
  selector: 'app-questions',
  standalone: true,
  imports: [IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './questions.component.html'
})
export class QuestionsComponent implements OnInit {
  private readonly testService = inject(VocationalTestService);
  private readonly analytics = inject(AnalyticsService);
  private readonly draft = inject(QuizDraftService);
  private readonly router = inject(Router);

  readonly options = LIKERT_OPTIONS;
  readonly questions = signal<Question[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly submitting = signal(false);

  readonly answers = computed(() => this.draft.draft().answers);
  readonly pageCount = computed(() => Math.max(1, Math.ceil(this.questions().length / QUESTIONS_PER_PAGE)));
  /** Tela atual (0 a 4), guardada no rascunho para a pessoa voltar de onde parou. */
  readonly page = computed(() => Math.min(this.draft.draft().current, this.pageCount() - 1));
  readonly pageQuestions = computed(() =>
    this.questions().slice(this.page() * QUESTIONS_PER_PAGE, (this.page() + 1) * QUESTIONS_PER_PAGE)
  );
  readonly answeredCount = computed(() => this.questions().filter((q) => this.answers()[q.id] !== undefined).length);
  readonly pageComplete = computed(() => this.pageQuestions().every((q) => this.answers()[q.id] !== undefined));
  readonly isLast = computed(() => this.page() === this.pageCount() - 1);
  readonly allAnswered = computed(
    () => this.questions().length > 0 && this.answeredCount() === this.questions().length
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

  /** Guarda a nota (0 a 4) da atividade no rascunho. */
  respond(questionId: number, score: number): void {
    if (!this.submitting()) {
      this.draft.answer(questionId, score);
    }
  }

  /** Próxima tela: só depois de responder as 6 atividades da tela atual. */
  next(): void {
    if (this.pageComplete() && !this.isLast()) {
      this.draft.goTo(this.page() + 1);
      this.scrollToTop();
    }
  }

  back(): void {
    if (this.page() === 0) {
      this.router.navigate(['/teste-vocacional']);
    } else {
      this.draft.goTo(this.page() - 1);
      this.scrollToTop();
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
        this.analytics.track('test_completed', {
          profile: result.topCategories.map((c) => c.category).join(','),
          tie: result.topCategories.length > 1
        });
        this.draft.clear();
        this.router.navigate(['/teste-vocacional/resultado', result.id]);
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível enviar suas respostas. Tente novamente.'));
        this.submitting.set(false);
      }
    });
  }

  private scrollToTop(): void {
    window.scrollTo({ top: 0 });
  }
}
