import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export interface RadarPoint {
  label: string;
  value: number;
}

const SIZE = 320;
const CENTER = SIZE / 2;
const RADIUS = 110;

/**
 * Gráfico de radar em SVG puro (sem biblioteca): cada eixo é um tipo RIASEC e o polígono liga os escores.
 * A escala vai de "min" (centro) a "max" (borda).
 */
@Component({
  selector: 'app-radar-chart',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <svg [attr.viewBox]="'0 0 ' + size + ' ' + size" class="h-auto w-full max-w-[360px]" role="img" [attr.aria-label]="ariaLabel()">
      @for (level of levels(); track level.value) {
        <polygon [attr.points]="level.points" fill="none" stroke="var(--border)" />
        <text [attr.x]="center + 4" [attr.y]="level.labelY" font-size="10" fill="var(--muted-foreground)">{{ level.value }}</text>
      }
      @for (axis of axes(); track axis.label) {
        <line [attr.x1]="center" [attr.y1]="center" [attr.x2]="axis.x" [attr.y2]="axis.y" stroke="var(--border)" />
        <text [attr.x]="axis.labelX" [attr.y]="axis.labelY" [attr.text-anchor]="axis.anchor" dominant-baseline="middle"
          font-size="12" fill="var(--foreground)">{{ axis.label }}</text>
      }
      <polygon [attr.points]="shape()" fill="var(--primary)" fill-opacity="0.1" stroke="var(--primary)" stroke-width="2" />
      @for (dot of dots(); track $index) {
        <circle [attr.cx]="dot.x" [attr.cy]="dot.y" r="3.5" fill="var(--primary)" />
      }
    </svg>
  `
})
export class RadarChartComponent {
  readonly points = input.required<RadarPoint[]>();
  readonly min = input(2);
  readonly max = input(10);

  readonly size = SIZE;
  readonly center = CENTER;

  private angle(index: number): number {
    return (-90 + (360 / this.points().length) * index) * (Math.PI / 180);
  }

  private position(index: number, ratio: number): { x: number; y: number } {
    const angle = this.angle(index);
    return { x: round(CENTER + RADIUS * ratio * Math.cos(angle)), y: round(CENTER + RADIUS * ratio * Math.sin(angle)) };
  }

  private ratio(value: number): number {
    const clamped = Math.min(Math.max(value, this.min()), this.max());
    return (clamped - this.min()) / (this.max() - this.min());
  }

  readonly axes = computed(() =>
    this.points().map((point, i) => {
      const end = this.position(i, 1);
      const label = this.position(i, 1.22);
      const anchor = Math.abs(label.x - CENTER) < 5 ? 'middle' : label.x > CENTER ? 'start' : 'end';
      return { label: point.label, x: end.x, y: end.y, labelX: label.x, labelY: label.y, anchor };
    })
  );

  readonly levels = computed(() => {
    const steps = [];
    for (let value = this.min(); value <= this.max(); value += 2) {
      const ratio = this.ratio(value);
      if (ratio === 0) continue;
      const points = this.points().map((_, i) => this.position(i, ratio)).map((p) => `${p.x},${p.y}`).join(' ');
      steps.push({ value, points, labelY: round(CENTER - RADIUS * ratio - 2) });
    }
    return steps;
  });

  readonly dots = computed(() => this.points().map((point, i) => this.position(i, this.ratio(point.value))));
  readonly shape = computed(() => this.dots().map((p) => `${p.x},${p.y}`).join(' '));
  readonly ariaLabel = computed(
    () => 'Gráfico de radar dos escores: ' + this.points().map((p) => `${p.label} ${p.value}`).join(', ')
  );
}

function round(value: number): number {
  return Math.round(value * 10) / 10;
}
