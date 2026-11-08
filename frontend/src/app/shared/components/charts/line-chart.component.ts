import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export interface ChartPoint {
  label: string;
  value: number;
}

const WIDTH = 600;
const HEIGHT = 160;
const PAD = { top: 20, right: 12, bottom: 24, left: 28 };

/** Gráfico de linha em SVG (ex.: testes realizados por dia), com o valor escrito acima de cada ponto diferente de zero. */
@Component({
  selector: 'app-line-chart',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg [attr.viewBox]="'0 0 ' + width + ' ' + height" class="h-auto w-full" role="img" [attr.aria-label]="ariaLabel()">
      @for (tick of yTicks(); track tick.value) {
        <line [attr.x1]="pad.left" [attr.x2]="width - pad.right" [attr.y1]="tick.y" [attr.y2]="tick.y" stroke="var(--border)" />
        <text [attr.x]="pad.left - 6" [attr.y]="tick.y + 4" text-anchor="end" font-size="11" fill="var(--muted-foreground)">{{ tick.value }}</text>
      }
      @for (label of xLabels(); track label.x) {
        <text [attr.x]="label.x" [attr.y]="height - 8" text-anchor="middle" font-size="11" fill="var(--muted-foreground)">{{ label.text }}</text>
      }
      <polyline [attr.points]="path()" fill="none" stroke="var(--primary)" stroke-width="2.5" stroke-linejoin="round" />
      @for (p of coords(); track $index) {
        <circle [attr.cx]="p.x" [attr.cy]="p.y" r="3" fill="var(--primary)"><title>{{ p.label }}: {{ p.value }}</title></circle>
        @if (p.value > 0) {
          <text [attr.x]="p.x" [attr.y]="p.y - 8" text-anchor="middle" font-size="11" font-weight="600" fill="var(--foreground)">{{ p.value }}</text>
        }
      }
    </svg>
  `
})
export class LineChartComponent {
  readonly points = input.required<ChartPoint[]>();
  readonly width = WIDTH;
  readonly height = HEIGHT;
  readonly pad = PAD;

  private readonly max = computed(() => niceMax(Math.max(0, ...this.points().map((p) => p.value))));

  readonly coords = computed(() => {
    const points = this.points();
    const innerW = WIDTH - PAD.left - PAD.right;
    const innerH = HEIGHT - PAD.top - PAD.bottom;
    const step = points.length > 1 ? innerW / (points.length - 1) : 0;
    return points.map((p, i) => ({
      ...p,
      x: Math.round((PAD.left + step * i) * 10) / 10,
      y: Math.round((PAD.top + innerH - (p.value / this.max()) * innerH) * 10) / 10
    }));
  });

  readonly path = computed(() => this.coords().map((p) => `${p.x},${p.y}`).join(' '));

  readonly yTicks = computed(() => {
    const innerH = HEIGHT - PAD.top - PAD.bottom;
    return [0, 0.5, 1].map((ratio) => ({
      value: Math.round(this.max() * ratio),
      y: Math.round(PAD.top + innerH - ratio * innerH)
    }));
  });

  /** No máximo ~6 rótulos no eixo X, para não sobrepor. */
  readonly xLabels = computed(() => {
    const coords = this.coords();
    const every = Math.max(1, Math.ceil(coords.length / 6));
    return coords.filter((_, i) => i % every === 0 || i === coords.length - 1).map((p) => ({ x: p.x, text: p.label }));
  });

  readonly ariaLabel = computed(() => this.points().map((p) => `${p.label}: ${p.value}`).join(', '));
}

/** Arredonda o topo do eixo para um número "redondo" (mínimo 4). */
export function niceMax(value: number): number {
  if (value <= 4) return 4;
  const magnitude = Math.pow(10, Math.floor(Math.log10(value)));
  return Math.ceil(value / magnitude) * magnitude;
}
