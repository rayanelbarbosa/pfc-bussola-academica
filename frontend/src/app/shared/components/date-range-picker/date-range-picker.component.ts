import { ChangeDetectionStrategy, Component, ElementRef, computed, inject, input, output, signal, viewChild } from '@angular/core';

import { IconComponent } from '../icon/icon.component';

/** Período escolhido: datas ISO (AAAA-MM-DD), as duas inclusive; as duas nulas = sem filtro de período. */
export interface DateRangeValue {
  from: string | null;
  to: string | null;
}

interface CalendarDay {
  iso: string;
  day: number;
  inMonth: boolean;
  disabled: boolean;
}

const MONTHS = ['janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho', 'julho', 'agosto', 'setembro', 'outubro',
  'novembro', 'dezembro'];
const WEEKDAYS = [
  { short: 'D', long: 'domingo' }, { short: 'S', long: 'segunda-feira' }, { short: 'T', long: 'terça-feira' },
  { short: 'Q', long: 'quarta-feira' }, { short: 'Q', long: 'quinta-feira' }, { short: 'S', long: 'sexta-feira' },
  { short: 'S', long: 'sábado' }
];

/** AAAA-MM-DD da data local (sem converter para UTC, que mudaria o dia à noite). */
export function toIso(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

function fromIso(iso: string): Date {
  const [year, month, day] = iso.split('-').map(Number);
  return new Date(year, month - 1, day);
}

function addDays(iso: string, days: number): string {
  const date = fromIso(iso);
  date.setDate(date.getDate() + days);
  return toIso(date);
}

/** 20/11/2026 */
export function formatBr(iso: string): string {
  const [year, month, day] = iso.split('-');
  return `${day}/${month}/${year}`;
}

let nextId = 0;

/**
 * Calendário de período (RF do #153): um botão que abre, logo abaixo, um calendário de um mês.
 * 1º clique marca o início, 2º clique marca o fim e aplica; dois cliques no mesmo dia filtram só aquele dia.
 * Datas futuras ficam desabilitadas. Funciona por teclado (setas, PageUp/PageDown, Enter, Esc).
 */
@Component({
  selector: 'app-date-range-picker',
  standalone: true,
  imports: [IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'relative inline-block',
    '(document:click)': 'onDocumentClick($event)',
    '(keydown.escape)': 'close(true)'
  },
  template: `
    <button #trigger type="button" class="field-input flex items-center gap-2 text-left"
      [attr.aria-label]="label() + ': ' + summary()" aria-haspopup="dialog" [attr.aria-expanded]="open()"
      [attr.aria-controls]="panelId" (click)="toggle()">
      <app-icon name="calendar" class="size-4 shrink-0 text-muted-foreground" />
      <span class="truncate">{{ summary() }}</span>
      <app-icon name="chevron-down" class="ml-auto size-4 shrink-0 text-muted-foreground" />
    </button>

    @if (open()) {
      <div [id]="panelId" role="dialog" [attr.aria-label]="'Escolher ' + label().toLowerCase()"
        class="absolute left-0 top-full z-30 mt-2 w-[19rem] rounded-xl border border-border bg-card p-3 shadow-lg">
        <div class="mb-2 flex items-center justify-between">
          <button type="button" class="btn btn-ghost btn-sm size-9 p-0" aria-label="Mês anterior" (click)="moveMonth(-1)">
            <app-icon name="chevron-left" class="size-4" />
          </button>
          <p class="text-sm font-semibold capitalize" aria-live="polite">{{ monthTitle() }}</p>
          <button type="button" class="btn btn-ghost btn-sm size-9 p-0" aria-label="Próximo mês" [disabled]="!canGoNext()"
            (click)="moveMonth(1)">
            <app-icon name="chevron-right" class="size-4" />
          </button>
        </div>

        <table class="w-full border-collapse text-center text-sm" [attr.aria-label]="monthTitle()">
          <thead>
            <tr>
              @for (weekday of weekdays; track $index) {
                <th scope="col" class="pb-1 text-xs font-medium text-muted-foreground"><abbr [title]="weekday.long">{{ weekday.short }}</abbr></th>
              }
            </tr>
          </thead>
          <tbody>
            @for (week of weeks(); track $index) {
              <tr>
                @for (day of week; track day.iso) {
                  <td class="p-0" [class.bg-accent]="inRange(day.iso)" [class.rounded-l-md]="isEdge(day.iso, 'start')"
                    [class.rounded-r-md]="isEdge(day.iso, 'end')">
                    <button type="button" [class]="dayClass(day)"
                      [disabled]="day.disabled" [attr.tabindex]="day.iso === focused() ? 0 : -1"
                      [attr.data-date]="day.iso" [attr.aria-label]="longLabel(day.iso)"
                      [attr.aria-pressed]="isEdge(day.iso, 'start') || isEdge(day.iso, 'end')"
                      (click)="pick(day.iso)" (mouseenter)="hovered.set(day.iso)" (focus)="hovered.set(day.iso)"
                      (keydown)="onDayKey($event, day.iso)">
                      {{ day.day }}
                    </button>
                  </td>
                }
              </tr>
            }
          </tbody>
        </table>

        <div class="mt-2 flex items-center justify-between gap-2">
          <p class="text-xs text-muted-foreground" aria-live="polite">
            {{ pendingStart() ? 'Agora clique na data final (ou no mesmo dia).' : 'Clique na data inicial.' }}
          </p>
          <button type="button" class="btn btn-link text-xs" (click)="clear()">Limpar</button>
        </div>
      </div>
    }
  `
})
export class DateRangePickerComponent {
  readonly from = input<string | null>(null);
  readonly to = input<string | null>(null);
  readonly label = input('Período');
  readonly rangeChange = output<DateRangeValue>();

  readonly panelId = `periodo-${nextId++}`;
  readonly weekdays = WEEKDAYS;
  readonly today = toIso(new Date());

  readonly open = signal(false);
  readonly pendingStart = signal<string | null>(null);
  readonly hovered = signal<string | null>(null);
  readonly focused = signal(this.today);
  /** Primeiro dia do mês mostrado. */
  readonly month = signal(this.today.slice(0, 8) + '01');

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly trigger = viewChild.required<ElementRef<HTMLButtonElement>>('trigger');

  readonly summary = computed(() => {
    const from = this.from();
    const to = this.to();
    if (!from || !to) return 'Todo o período';
    return from === to ? formatBr(from) : `${formatBr(from)} a ${formatBr(to)}`;
  });

  readonly monthTitle = computed(() => {
    const date = fromIso(this.month());
    return `${MONTHS[date.getMonth()]} de ${date.getFullYear()}`;
  });

  readonly canGoNext = computed(() => this.month() < this.today.slice(0, 8) + '01');

  /** Semanas do mês (domingo a sábado), com os dias vizinhos completando a primeira e a última. */
  readonly weeks = computed<CalendarDay[][]>(() => {
    const first = fromIso(this.month());
    let cursor = addDays(this.month(), -first.getDay());
    const weeks: CalendarDay[][] = [];
    do {
      const week: CalendarDay[] = [];
      for (let i = 0; i < 7; i++) {
        week.push({ iso: cursor, day: fromIso(cursor).getDate(), inMonth: cursor.slice(0, 7) === this.month().slice(0, 7),
          disabled: cursor > this.today });
        cursor = addDays(cursor, 1);
      }
      weeks.push(week);
    } while (cursor.slice(0, 7) === this.month().slice(0, 7));
    return weeks;
  });

  /** Início e fim que aparecem pintados: o período em escolha (com o dia sob o mouse) ou o período aplicado. */
  private readonly shown = computed<[string, string] | null>(() => {
    const start = this.pendingStart();
    if (start) {
      const end = this.hovered() ?? start;
      return start <= end ? [start, end] : [end, start];
    }
    const from = this.from();
    const to = this.to();
    return from && to ? [from, to] : null;
  });

  toggle(): void {
    if (this.open()) {
      this.close(false);
      return;
    }
    const start = this.to() ?? this.today;
    this.month.set(start.slice(0, 8) + '01');
    this.focused.set(start);
    this.pendingStart.set(null);
    this.open.set(true);
    this.focusDay(start);
  }

  close(returnFocus: boolean): void {
    if (!this.open()) return;
    this.open.set(false);
    this.pendingStart.set(null);
    if (returnFocus) this.trigger().nativeElement.focus();
  }

  moveMonth(delta: number): void {
    const date = fromIso(this.month());
    date.setMonth(date.getMonth() + delta);
    this.month.set(toIso(date));
    this.focused.set(this.month() < this.today ? this.month() : this.today);
  }

  pick(iso: string): void {
    const start = this.pendingStart();
    if (!start) {
      this.pendingStart.set(iso);
      this.hovered.set(iso);
      return;
    }
    const [from, to] = start <= iso ? [start, iso] : [iso, start];
    this.rangeChange.emit({ from, to });
    this.close(true);
  }

  clear(): void {
    this.rangeChange.emit({ from: null, to: null });
    this.close(true);
  }

  inRange(iso: string): boolean {
    const range = this.shown();
    return !!range && iso >= range[0] && iso <= range[1];
  }

  isEdge(iso: string, edge: 'start' | 'end'): boolean {
    const range = this.shown();
    return !!range && iso === (edge === 'start' ? range[0] : range[1]);
  }

  longLabel(iso: string): string {
    const date = fromIso(iso);
    return `${date.getDate()} de ${MONTHS[date.getMonth()]} de ${date.getFullYear()}`;
  }

  onDayKey(event: KeyboardEvent, iso: string): void {
    const moves: Record<string, number> = { ArrowLeft: -1, ArrowRight: 1, ArrowUp: -7, ArrowDown: 7 };
    let target: string | null = null;
    if (event.key in moves) {
      target = addDays(iso, moves[event.key]);
    } else if (event.key === 'PageUp' || event.key === 'PageDown') {
      const date = fromIso(iso);
      date.setMonth(date.getMonth() + (event.key === 'PageUp' ? -1 : 1));
      target = toIso(date);
    }
    if (!target) return;
    event.preventDefault();
    if (target > this.today) target = this.today;
    this.focused.set(target);
    this.hovered.set(target);
    this.month.set(target.slice(0, 8) + '01');
    this.focusDay(target);
  }

  /** Classes do botão do dia: pontas do período em destaque, dias de outro mês apagados, futuros desabilitados. */
  dayClass(day: CalendarDay): string {
    const edge = this.isEdge(day.iso, 'start') || this.isEdge(day.iso, 'end');
    return [
      'grid size-9 w-full place-items-center rounded-md text-sm',
      edge ? 'bg-primary text-primary-foreground' : day.disabled ? '' : 'hover:bg-muted',
      !edge && !day.inMonth ? 'text-muted-foreground' : '',
      day.iso === this.today ? 'font-semibold underline underline-offset-2' : '',
      day.disabled ? 'cursor-not-allowed opacity-40' : ''
    ].filter(Boolean).join(' ');
  }

  /** Depois de redesenhar o mês, leva o foco do teclado para o dia informado. */
  private focusDay(iso: string): void {
    setTimeout(() => this.host.nativeElement.querySelector<HTMLButtonElement>(`[data-date="${iso}"]`)?.focus());
  }

  onDocumentClick(event: MouseEvent): void {
    if (this.open() && !this.host.nativeElement.contains(event.target as Node)) {
      this.close(false);
    }
  }
}
