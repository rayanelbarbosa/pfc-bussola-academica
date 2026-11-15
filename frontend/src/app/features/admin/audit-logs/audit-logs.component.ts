import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AUDIT_ACTION_LABELS, AuditIntegrity, AuditLog, Page } from '../../../core/models/audit.model';
import { AuditService } from '../../../core/services/audit.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { IconComponent } from '../../../shared/components/icon/icon.component';

/** Consulta dos logs de auditoria (somente administrador), com filtros e paginação. */
@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [DatePipe, FormsModule, IconComponent],
  templateUrl: './audit-logs.component.html'
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

  /** Confere a corrente de hashes e recarrega a lista (a verificação também vira um registro). */
  verifyIntegrity(): void {
    this.verifying.set(true);
    this.error.set(null);
    this.auditService.verifyIntegrity().subscribe({
      next: (result) => {
        this.integrity.set(result);
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
