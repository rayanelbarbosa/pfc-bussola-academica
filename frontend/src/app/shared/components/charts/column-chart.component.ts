import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

import { ChartPoint, niceMax } from './line-chart.component';

/** Colunas verticais (ex.: novos cadastros por semana). */
@Component({
  selector: 'app-column-chart',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="flex h-32 items-end gap-2" role="img" [attr.aria-label]="ariaLabel()">
      @for (p of points(); track p.label) {
        <div class="flex min-w-0 flex-1 flex-col items-center gap-1">
          <span class="text-xs font-semibold">{{ p.value }}</span>
          <div class="w-full max-w-10 rounded-t-md bg-primary" [style.height.px]="height(p.value)" [attr.title]="p.label + ': ' + p.value"></div>
          <span class="w-full truncate text-center text-[11px] text-muted-foreground">{{ p.label }}</span>
        </div>
      }
    </div>
  `
})
export class ColumnChartComponent {
  readonly points = input.required<ChartPoint[]>();
  private readonly max = computed(() => niceMax(Math.max(0, ...this.points().map((p) => p.value))));
  readonly ariaLabel = computed(() => this.points().map((p) => `${p.label}: ${p.value}`).join(', '));

  /** Altura em px (máx. 80), com 2 px mínimos para a coluna zero continuar visível. */
  height(value: number): number {
    return Math.max(2, Math.round((value / this.max()) * 80));
  }
}
