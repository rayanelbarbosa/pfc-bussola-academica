import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { MAX_SCORE, VocationalTestResult } from '../../../core/models/vocational-test.model';
import { getCategoryInfo } from '../../../core/models/riasec-category';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';

interface ScoreBar {
  label: string;
  score: number;
  percent: number;
  color: string;
}

/** Tela de resultado do teste (protótipo 15 - Resultado). */
@Component({
  selector: 'app-result',
  standalone: true,
  imports: [RouterLink, TopBarComponent],
  templateUrl: './result.component.html',
  styleUrl: './result.component.css'
})
export class ResultComponent implements OnInit {
  private readonly service = inject(VocationalTestService);

  /** Vem da rota /teste-vocacional/resultado/:id */
  readonly id = input.required<string>();

  readonly maxScore = MAX_SCORE;
  readonly result = signal<VocationalTestResult | null>(null);
  readonly error = signal<string | null>(null);

  readonly topCategories = computed(() => this.result()?.topCategories ?? []);
  readonly topLabels = computed(() => joinWithAnd(this.topCategories().map((c) => c.label)));
  readonly isTie = computed(() => this.topCategories().length > 1);
  readonly topColor = computed(() => getCategoryInfo(this.topCategories()[0]?.category ?? '').color);
  readonly highestScore = computed(() => this.topCategories()[0]?.score ?? 0);

  readonly scoreBars = computed<ScoreBar[]>(() =>
    (this.result()?.scores ?? []).map((s) => ({
      label: s.label,
      score: s.score,
      percent: (s.score / MAX_SCORE) * 100,
      color: getCategoryInfo(s.category).color
    }))
  );

  readonly title = computed(() =>
    this.isTie() ? `Seus lados ${this.topLabels()} se destacam` : `Seu lado ${this.topLabels()} se destaca`
  );

  readonly description = computed(() => {
    const points = `(${this.highestScore()} de ${MAX_SCORE})`;
    if (this.isTie()) {
      return `${this.topLabels()} empataram com a maior pontuação ${points} entre as 6 categorias do modelo RIASEC.`;
    }
    const top = this.topCategories()[0];
    return top
      ? `${top.label} teve a maior pontuação ${points} entre as 6 categorias do modelo RIASEC — ${getCategoryInfo(top.category).description}`
      : '';
  });

  ngOnInit(): void {
    const id = Number(this.id());
    if (!Number.isInteger(id) || id <= 0) {
      this.error.set('Resultado inválido.');
      return;
    }
    this.service.getResult(id).subscribe({
      next: (result) => this.result.set(result),
      error: (err) => this.error.set(getErrorMessage(err, 'Não foi possível carregar o resultado.'))
    });
  }
}

function joinWithAnd(items: string[]): string {
  if (items.length <= 1) {
    return items.join('');
  }
  return `${items.slice(0, -1).join(', ')} e ${items[items.length - 1]}`;
}
