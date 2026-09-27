import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { LIKERT_OPTIONS, Question } from '../../../core/models/vocational-test.model';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { getErrorMessage } from '../../../core/services/api-error';

/** Questionário vocacional: uma pergunta por tela, com barra de progresso (protótipo 03 - Pergunta). */
@Component({
  selector: 'app-questionnaire',
  standalone: true,
  templateUrl: './questionnaire.component.html',
  styleUrl: './questionnaire.component.css'
})
export class QuestionnaireComponent implements OnInit {
  private readonly service = inject(VocationalTestService);
  private readonly router = inject(Router);

  readonly options = LIKERT_OPTIONS;
  readonly questions = signal<Question[]>([]);
  readonly currentIndex = signal(0);
  readonly answers = signal<ReadonlyMap<number, number>>(new Map());
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly total = computed(() => this.questions().length);
  readonly currentQuestion = computed(() => this.questions()[this.currentIndex()]);
  readonly currentScore = computed(() => {
    const question = this.currentQuestion();
    return question ? this.answers().get(question.id) : undefined;
  });
  readonly isLast = computed(() => this.currentIndex() === this.total() - 1);
  readonly progress = computed(() =>
    this.total() === 0 ? 0 : Math.round(((this.currentIndex() + 1) / this.total()) * 100)
  );

  ngOnInit(): void {
    this.service.getQuestions().subscribe({
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

  select(score: number): void {
    const question = this.currentQuestion();
    if (!question) {
      return;
    }
    const updated = new Map(this.answers());
    updated.set(question.id, score);
    this.answers.set(updated);
  }

  previous(): void {
    this.currentIndex.update((i) => Math.max(0, i - 1));
  }

  next(): void {
    if (this.currentScore() === undefined) {
      return;
    }
    if (this.isLast()) {
      this.submit();
    } else {
      this.currentIndex.update((i) => i + 1);
    }
  }

  private submit(): void {
    this.submitting.set(true);
    this.error.set(null);
    const answers = this.questions().map((q) => ({ questionId: q.id, score: this.answers().get(q.id)! }));

    this.service.submitAnswers({ answers }).subscribe({
      next: (result) => this.router.navigate(['/teste-vocacional/resultado', result.id]),
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível calcular seu resultado. Tente novamente.'));
        this.submitting.set(false);
      }
    });
  }
}
