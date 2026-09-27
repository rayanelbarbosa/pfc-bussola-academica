import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AUDIT_ACTION_LABELS, AuditLog, Page } from '../../../core/models/audit.model';
import { AuditService } from '../../../core/services/audit.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';

/** Consulta dos logs de auditoria (somente administrador), com filtros e paginação. */
@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [DatePipe, FormsModule, TopBarComponent],
  templateUrl: './audit-logs.component.html',
  styleUrl: './audit-logs.component.css'
})
export class AuditLogsComponent implements OnInit {
  private readonly auditService = inject(AuditService);

  readonly pageSize = 20;
  readonly labels = AUDIT_ACTION_LABELS;
  readonly actions = Object.keys(AUDIT_ACTION_LABELS);

  action = '';
  email = '';
  readonly page = signal<Page<AuditLog> | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.load(0);
  }

  search(): void {
    this.load(0);
  }

  clear(): void {
    this.action = '';
    this.email = '';
    this.load(0);
  }

  load(pageNumber: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.auditService.search({ action: this.action, email: this.email, page: pageNumber, size: this.pageSize }).subscribe({
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

  label(action: string): string {
    return this.labels[action] ?? action;
  }
}
