import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AUDIT_ACTION_LABELS, AuditIntegrity, AuditLog, Page } from '../../../core/models/audit.model';
import { AuditService } from '../../../core/services/audit.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { DateRangePickerComponent, DateRangeValue } from '../../../shared/components/date-range-picker/date-range-picker.component';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { AnalyticsService } from '../../../core/services/analytics.service';

/** Consulta dos logs de auditoria (somente administrador), com filtros e paginação. */
@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [DatePipe, FormsModule, IconComponent, DateRangePickerComponent],
  templateUrl: './audit-logs.component.html'
})
export class AuditLogsComponent implements OnInit {
  private readonly auditService = inject(AuditService);
  private readonly analytics = inject(AnalyticsService);

  readonly pageSize = 20;
  readonly labels = AUDIT_ACTION_LABELS;
  readonly actions = Object.keys(AUDIT_ACTION_LABELS);

  action = '';
  email = '';
  /** Período dos eventos escolhido no calendário (aplica na hora, sem precisar clicar em Filtrar). */
  readonly range = signal<DateRangeValue>({ from: null, to: null });
  readonly page = signal<Page<AuditLog> | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  /** Resultado da última verificação de integridade (corrente de hashes). */
  readonly integrity = signal<AuditIntegrity | null>(null);
  readonly verifying = signal(false);

  ngOnInit(): void {
    this.load(0);
  }

  search(): void {
    this.load(0);
  }

  clear(): void {
    this.action = '';
    this.email = '';
    this.range.set({ from: null, to: null });
    this.load(0);
  }

  selectRange(range: DateRangeValue): void {
    this.range.set(range);
    this.analytics.track('period_filter_applied', { screen: 'audit', source: 'calendar', from: range.from, to: range.to });
    this.load(0);
  }

  load(pageNumber: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.auditService.search({ action: this.action, email: this.email, from: this.range().from, to: this.range().to, page: pageNumber,
      size: this.pageSize }).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível carregar os logs.'));
        this.loading.set(false);
      }
    });
  }

  /** Confere a corrente de hashes e recarrega a lista (a verificação também vira um registro). */
  verifyIntegrity(): void {
    this.verifying.set(true);
    this.error.set(null);
    this.auditService.verifyIntegrity().subscribe({
      next: (result) => {
        this.integrity.set(result);
        this.analytics.track('audit_integrity_checked', { intact: result.intact, checked_records: result.checkedRecords });
        this.verifying.set(false);
        this.load(0);
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível verificar a integridade da auditoria.'));
        this.verifying.set(false);
      }
    });
  }

  label(action: string): string {
    return this.labels[action] ?? action;
  }
}
