import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

import { ChartPoint, niceMax } from './line-chart.component';

/**
 * Colunas verticais (ex.: novos cadastros por dia ou por mês). Com muitas colunas, os números em cima somem e só
 * alguns rótulos de data aparecem, para não sobrepor (o valor de cada coluna continua no título, ao passar o mouse).
 */
@Component({
  selector: 'app-column-chart',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="flex h-32 items-end gap-2" role="img" [attr.aria-label]="ariaLabel()">
      @for (p of points(); track p.label; let i = $index) {
        <div class="flex min-w-0 flex-1 flex-col items-center gap-1">
          <span class="h-4 text-xs font-semibold">{{ showValues() ? p.value : '' }}</span>
          <div class="w-full max-w-10 rounded-t-md bg-primary" [style.height.px]="height(p.value)" [attr.title]="p.label + ': ' + p.value"></div>
          <span class="h-4 w-full whitespace-nowrap text-center text-[11px] text-muted-foreground">{{ showLabel(i) ? p.label : '' }}</span>
        </div>
      }
    </div>
  `
})
export class ColumnChartComponent {
  readonly points = input.required<ChartPoint[]>();
  private readonly max = computed(() => niceMax(Math.max(0, ...this.points().map((p) => p.value))));
  readonly ariaLabel = computed(() => this.points().map((p) => `${p.label}: ${p.value}`).join(', '));
  /** Números em cima das colunas só até 14 colunas. */
  readonly showValues = computed(() => this.points().length <= 14);
  private readonly labelEvery = computed(() => Math.max(1, Math.ceil(this.points().length / 7)));

  /** No máximo ~7 rótulos de data: a cada N colunas, mais a última. */
  showLabel(index: number): boolean {
    return index % this.labelEvery() === 0 || index === this.points().length - 1;
  }

  /** Altura em px (máx. 80), com 2 px mínimos para a coluna zero continuar visível. */
  height(value: number): number {
    return Math.max(2, Math.round((value / this.max()) * 80));
  }
}
