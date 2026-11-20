import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { AuditLogsComponent } from './audit-logs.component';
import { AuditService } from '../../../core/services/audit.service';

describe('AuditLogsComponent', () => {
  let fixture: ComponentFixture<AuditLogsComponent>;
  let auditService: jasmine.SpyObj<AuditService>;

  beforeEach(async () => {
    auditService = jasmine.createSpyObj('AuditService', ['search', 'verifyIntegrity']);
    auditService.search.and.returnValue(
      of({
        content: [
          {
            id: 1,
            createdAt: '2026-09-27T10:00:00',
            userId: 2,
            userEmail: 'aluna@teste.com',
            action: 'ACCESS_DENIED',
            resource: 'GET /api/admin/audit-logs',
            details: null,
            ipAddress: '127.0.0.1',
            success: false
          }
        ],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1
      })
    );
    await TestBed.configureTestingModule({
      imports: [AuditLogsComponent],
      providers: [provideRouter([]), provideHttpClient(), { provide: AuditService, useValue: auditService }]
    }).compileComponents();
    fixture = TestBed.createComponent(AuditLogsComponent);
    fixture.detectChanges();
  });

  it('lists the logs with readable action names', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelectorAll('tbody tr').length).toBe(1);
    expect(el.textContent).toContain('Acesso negado');
    expect(el.textContent).toContain('Falha');
  });

  it('applies the filters', () => {
    fixture.componentInstance.action = 'LOGIN_FAILURE';
    fixture.componentInstance.email = 'aluna';
    fixture.componentInstance.search();

    expect(auditService.search).toHaveBeenCalledWith({ action: 'LOGIN_FAILURE', email: 'aluna', from: null, to: null, page: 0, size: 20 });
  });

  it('filters by the period of the events from the calendar', () => {
    const component = fixture.componentInstance;
    component.selectRange({ from: '2026-11-20', to: '2026-11-20' });
    expect(auditService.search).toHaveBeenCalledWith(jasmine.objectContaining({ from: '2026-11-20', to: '2026-11-20', page: 0 }));
    component.clear();
    expect(component.range()).toEqual({ from: null, to: null });
  });

  it('clears the filters and shows an error when loading fails', () => {
    const component = fixture.componentInstance;
    component.action = 'LOGIN_FAILURE';
    component.email = 'a@b.com';
    component.clear();
    expect(component.action).toBe('');
    expect(auditService.search).toHaveBeenCalledWith(jasmine.objectContaining({ action: '', email: '', page: 0 }));

    auditService.search.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    component.search();
    expect(component.error()).toBeTruthy();
    expect(component.loading()).toBeFalse();
    expect(component.label('UNKNOWN_ACTION')).toBe('UNKNOWN_ACTION');
  });

  it('verifies the integrity and shows whether the chain is intact or where it broke', () => {
    const component = fixture.componentInstance;
    const el: HTMLElement = fixture.nativeElement;
    auditService.verifyIntegrity.and.returnValue(of({
      intact: true, checkedRecords: 42, brokenAtId: null, message: 'Corrente íntegra: 42 registros conferidos.',
      checkedAt: '2026-11-15T16:40:00'
    }));
    auditService.search.calls.reset();

    component.verifyIntegrity();
    fixture.detectChanges();

    expect(el.textContent).toContain('Auditoria íntegra');
    expect(el.textContent).toContain('42 registros');
    // a lista é recarregada para mostrar o registro da própria verificação
    expect(auditService.search).toHaveBeenCalled();

    auditService.verifyIntegrity.and.returnValue(of({
      intact: false, checkedRecords: 10, brokenAtId: 11, message: 'O registro 11 foi alterado depois de gravado.',
      checkedAt: '2026-11-15T16:41:00'
    }));
    component.verifyIntegrity();
    fixture.detectChanges();
    expect(el.querySelector('[role="alert"]')?.textContent).toContain('O registro 11 foi alterado');

    auditService.verifyIntegrity.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    component.verifyIntegrity();
    expect(component.error()).toContain('integridade');
    expect(component.verifying()).toBeFalse();
  });
});
