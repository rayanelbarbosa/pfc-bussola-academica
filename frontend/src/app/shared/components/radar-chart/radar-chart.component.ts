import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export interface RadarPoint {
  label: string;
  value: number;
  /** Cor do ponto e do rótulo (opcional; padrão: cor primária). */
  color?: string;
}

const SIZE = 320;
const CENTER = SIZE / 2;
const RADIUS = 110;
/** Margem lateral do viewBox: os rótulos dos eixos da esquerda e da direita ficam fora do círculo e não podem ser cortados. */
const PAD_X = 80;
const PAD_Y = 16;

/**
 * Gráfico de radar em SVG puro (sem biblioteca): cada eixo é um tipo RIASEC e o polígono liga os escores.
 * A escala vai de "min" (centro) a "max" (borda).
 */
@Component({
  selector: 'app-radar-chart',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  // sem largura no host, o SVG (w-full) encolhia dentro de containers flex e os rótulos ficavam ilegíveis
  host: { class: 'block w-full max-w-[460px]' },
  template: `
    <svg [attr.viewBox]="viewBox" class="h-auto w-full" role="img" [attr.aria-label]="ariaLabel()">
      @for (level of levels(); track level.value) {
        <polygon [attr.points]="level.points" fill="none" stroke="var(--border)" />
        @if (level.value < max()) {
          <text [attr.x]="center + 4" [attr.y]="level.labelY" font-size="10" fill="var(--muted-foreground)">{{ level.value }}</text>
        }
      }
      @for (axis of axes(); track axis.label) {
        <line [attr.x1]="center" [attr.y1]="center" [attr.x2]="axis.x" [attr.y2]="axis.y" stroke="var(--border)" />
        <text [attr.x]="axis.labelX" [attr.y]="axis.labelY" [attr.text-anchor]="axis.anchor" dominant-baseline="middle"
          font-size="13" font-weight="600" [attr.fill]="axis.color">{{ axis.label }}</text>
      }
      <polygon [attr.points]="shape()" fill="var(--primary)" fill-opacity="0.1" stroke="var(--primary)" stroke-width="2" />
      @for (dot of dots(); track $index) {
        <circle [attr.cx]="dot.x" [attr.cy]="dot.y" r="5" [attr.fill]="dot.color" stroke="var(--card)" stroke-width="1.5" />
      }
    </svg>
  `
})
export class RadarChartComponent {
  readonly points = input.required<RadarPoint[]>();
  readonly min = input(2);
  readonly max = input(10);

  readonly center = CENTER;
  readonly viewBox = `${-PAD_X} ${-PAD_Y} ${SIZE + 2 * PAD_X} ${SIZE + 2 * PAD_Y}`;

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
      const label = this.position(i, 1.12);
      const anchor = Math.abs(label.x - CENTER) < 5 ? 'middle' : label.x > CENTER ? 'start' : 'end';
      const color = point.color ?? 'var(--foreground)';
      return { label: point.label, x: end.x, y: end.y, labelX: label.x, labelY: label.y, anchor, color };
    })
  );

  readonly levels = computed(() => {
    const steps = [];
    for (let value = this.min(); value <= this.max(); value += 2) {
      const ratio = this.ratio(value);
      // sem o nível 0 (centro); o rótulo do nível máximo ficaria em cima do nome do eixo do topo
      if (ratio === 0) continue;
      const points = this.points().map((_, i) => this.position(i, ratio)).map((p) => `${p.x},${p.y}`).join(' ');
      steps.push({ value, points, labelY: round(CENTER - RADIUS * ratio - 2) });
    }
    return steps;
  });

  readonly dots = computed(() =>
    this.points().map((point, i) => ({ ...this.position(i, this.ratio(point.value)), color: point.color ?? 'var(--primary)' }))
  );
  readonly shape = computed(() => this.dots().map((p) => `${p.x},${p.y}`).join(' '));
  readonly ariaLabel = computed(
    () => 'Gráfico de radar dos escores: ' + this.points().map((p) => `${p.label} ${p.value}`).join(', ')
  );
}

function round(value: number): number {
  return Math.round(value * 10) / 10;
}
