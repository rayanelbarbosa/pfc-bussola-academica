import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';

import { DASHBOARD_PERIODS, Dashboard, DailyCount } from '../../../core/models/dashboard.model';
import { getCategoryInfo } from '../../../core/models/riasec-category';
import { getErrorMessage } from '../../../core/services/api-error';
import { DashboardService } from '../../../core/services/dashboard.service';
import { ChartPoint } from '../../../shared/components/charts/line-chart.component';
import { BarListComponent } from '../../../shared/components/charts/bar-list.component';
import { ColumnChartComponent } from '../../../shared/components/charts/column-chart.component';
import { LineChartComponent } from '../../../shared/components/charts/line-chart.component';
import { DateRangePickerComponent, DateRangeValue, formatBr } from '../../../shared/components/date-range-picker/date-range-picker.component';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { AnalyticsService } from '../../../core/services/analytics.service';

const MAX_DAYS_PER_DAY_CHART = 31;
const MONTHS = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];

/** Soma as contagens diárias por mês, na ordem: [{ label: 'jun/26', value: 85 }, ...]. */
export function groupByMonth(items: DailyCount[]): ChartPoint[] {
  const totals = new Map<string, number>();
  items.forEach((item) => {
    const month = item.date.slice(0, 7);
    totals.set(month, (totals.get(month) ?? 0) + item.count);
  });
  return [...totals.entries()].map(([month, value]) => {
    const [year, number] = month.split('-');
    return { label: `${MONTHS[Number(number) - 1]}/${year.slice(2)}`, value };
  });
}

/** Dashboard administrativo (RF15): big numbers e gráficos de uso da plataforma, com filtro de período. */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [IconComponent, LineChartComponent, BarListComponent, ColumnChartComponent, DateRangePickerComponent],
  providers: [DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  private readonly service = inject(DashboardService);
  private readonly analytics = inject(AnalyticsService);
  private readonly datePipe = inject(DatePipe);

  readonly periods = DASHBOARD_PERIODS;
  readonly formatBr = formatBr;
  /** Atalho marcado (7, 30 ou 90 dias); nulo = nenhum. Sem atalho e sem calendário, o dashboard mostra todo o período. */
  readonly days = signal<number | null>(null);
  /** Período escolhido no calendário; ao escolher, desmarca o atalho. */
  readonly range = signal<DateRangeValue>({ from: null, to: null });
  /** "01/11/2026 a 20/11/2026" (ou só a data, quando é um dia), do período que o back-end calculou. */
  readonly periodLabel = computed(() => {
    const d = this.data();
    if (!d?.startDate || !d.endDate) return '';
    return d.startDate === d.endDate ? formatBr(d.startDate) : `${formatBr(d.startDate)} a ${formatBr(d.endDate)}`;
  });
  readonly data = signal<Dashboard | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  /** Até 31 dias, um ponto por dia; em períodos maiores, um por mês (por dia vira um borrão de rótulos). */
  readonly byMonth = computed(() => (this.data()?.periodDays ?? 0) > MAX_DAYS_PER_DAY_CHART);
  readonly testsChart = computed(() => this.series(this.data()?.testsPerDay ?? []));
  readonly usersChart = computed(() => this.series(this.data()?.newUsersPerDay ?? []));
  readonly profiles = computed(() =>
    (this.data()?.profileDistribution ?? []).map((item) => ({
      key: item.key,
      label: item.label,
      value: item.count,
      color: `var(--riasec-${getCategoryInfo(item.key)?.letter.toLowerCase()})`
    }))
  );
  readonly areas = computed(() =>
    [...(this.data()?.areaDistribution ?? [])]
      .sort((a, b) => b.count - a.count)
      .map((item) => ({ key: item.key, label: item.label, value: item.count }))
  );

  ngOnInit(): void {
    this.load();
  }

  /** Marca o atalho (e limpa o calendário); clicar de novo no atalho marcado desmarca e volta a todo o período. */
  selectPeriod(days: number): void {
    this.days.set(this.days() === days ? null : days);
    this.range.set({ from: null, to: null });
    this.analytics.track('period_filter_applied', { screen: 'dashboard', source: 'quick', days: this.days() });
    this.load();
  }

  /** Período do calendário (desmarca o atalho); "Limpar" volta a todo o período. */
  selectRange(range: DateRangeValue): void {
    this.days.set(null);
    this.range.set(range);
    this.analytics.track('period_filter_applied', { screen: 'dashboard', source: 'calendar', from: range.from, to: range.to });
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.getMetrics(this.days(), this.range()).subscribe({
      next: (data) => {
        this.data.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível carregar os indicadores.'));
        this.loading.set(false);
      }
    });
  }

  /** Variação em relação ao período anterior de mesmo tamanho, ex.: "+3 em relação aos 30 dias anteriores". */
  variation(current: number, previous: number): string {
    const diff = current - previous;
    const sign = diff > 0 ? '+' : '';
    const days = this.data()?.periodDays ?? 0;
    return `${sign}${diff} em relação ${days === 1 ? 'ao dia anterior' : `aos ${days} dias anteriores`}`;
  }

  private series(items: DailyCount[]): ChartPoint[] {
    return this.byMonth()
      ? groupByMonth(items)
      : items.map((item) => ({ label: this.datePipe.transform(item.date, 'dd/MM') ?? item.date, value: item.count }));
  }
}
