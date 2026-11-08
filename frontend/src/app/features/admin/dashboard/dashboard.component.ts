import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';

import { DASHBOARD_PERIODS, Dashboard, DailyCount } from '../../../core/models/dashboard.model';
import { getCategoryInfo } from '../../../core/models/riasec-category';
import { getErrorMessage } from '../../../core/services/api-error';
import { DashboardService } from '../../../core/services/dashboard.service';
import { BarListComponent } from '../../../shared/components/charts/bar-list.component';
import { ColumnChartComponent } from '../../../shared/components/charts/column-chart.component';
import { LineChartComponent } from '../../../shared/components/charts/line-chart.component';
import { IconComponent } from '../../../shared/components/icon/icon.component';

/** Dashboard administrativo (RF15): big numbers e gráficos de uso da plataforma, com filtro de período. */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [IconComponent, LineChartComponent, BarListComponent, ColumnChartComponent],
  providers: [DatePipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  private readonly service = inject(DashboardService);
  private readonly datePipe = inject(DatePipe);

  readonly periods = DASHBOARD_PERIODS;
  readonly days = signal<number>(30);
  readonly data = signal<Dashboard | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly testsPerDay = computed(() => this.toPoints(this.data()?.testsPerDay ?? [], 'dd/MM'));
  readonly usersPerWeek = computed(() => this.toPoints(this.data()?.newUsersPerWeek ?? [], 'dd/MM'));
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

  selectPeriod(days: number): void {
    this.days.set(days);
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.getMetrics(this.days()).subscribe({
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

  /** Texto da variação em relação ao período anterior, ex.: "+3 em relação aos 30 dias anteriores". */
  variation(current: number, previous: number): string {
    const diff = current - previous;
    const sign = diff > 0 ? '+' : '';
    return `${sign}${diff} em relação aos ${this.days()} dias anteriores`;
  }

  private toPoints(items: DailyCount[], format: string) {
    return items.map((item) => ({ label: this.datePipe.transform(item.date, format) ?? item.date, value: item.count }));
  }
}
