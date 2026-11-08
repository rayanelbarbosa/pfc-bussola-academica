import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export interface BarItem {
  key: string;
  label: string;
  value: number;
  /** Cor da barra (variável CSS); padrão: violeta do tema. */
  color?: string;
}

/** Barras horizontais com rótulo e valor (ex.: distribuição dos perfis e das áreas). */
@Component({
  selector: 'app-bar-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ul class="space-y-2">
      @for (item of items(); track item.key) {
        <li>
          <div class="mb-0.5 flex justify-between gap-3 text-xs">
            <span>{{ item.label }}</span><span class="font-semibold">{{ item.value }}</span>
          </div>
          <div class="h-1.5 overflow-hidden rounded-full bg-muted" role="presentation">
            <div class="h-full rounded-full transition-all" [style.width.%]="percent(item.value)"
              [style.background]="item.color ?? 'var(--primary)'"></div>
          </div>
        </li>
      }
    </ul>
  `
})
export class BarListComponent {
  readonly items = input.required<BarItem[]>();
  private readonly max = computed(() => Math.max(1, ...this.items().map((i) => i.value)));

  percent(value: number): number {
    return Math.round((value / this.max()) * 100);
  }
}
