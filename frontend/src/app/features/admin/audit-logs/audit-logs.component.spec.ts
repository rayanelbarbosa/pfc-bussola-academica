import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';

import { AuditLogsComponent } from './audit-logs.component';
import { AuditService } from '../../../core/services/audit.service';

describe('AuditLogsComponent', () => {
  let fixture: ComponentFixture<AuditLogsComponent>;
  let auditService: jasmine.SpyObj<AuditService>;

  beforeEach(async () => {
    auditService = jasmine.createSpyObj('AuditService', ['search']);
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

    expect(auditService.search).toHaveBeenCalledWith({ action: 'LOGIN_FAILURE', email: 'aluna', page: 0, size: 20 });
  });
});
