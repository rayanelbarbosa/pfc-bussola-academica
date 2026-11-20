import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { AdminUser, AdminUserDetail, AdminUserService } from '../../../core/services/admin-user.service';
import { authResponse } from '../../../core/testing/auth-fixtures';
import { UsersComponent } from './users.component';

const ME: AdminUser = { id: 1, name: 'Marina Alves', role: 'ADMIN', createdAt: '2026-09-27T10:00:00' };
const STUDENT: AdminUser = { id: 2, name: 'João', role: 'STUDENT', createdAt: '2026-11-01T10:00:00' };

describe('UsersComponent', () => {
  let fixture: ComponentFixture<UsersComponent>;
  let service: jasmine.SpyObj<AdminUserService>;

  beforeEach(async () => {
    const admin = authResponse('ADMIN');
    localStorage.setItem('bussola.token', admin.token);
    localStorage.setItem('bussola.user', JSON.stringify(admin.user));
    service = jasmine.createSpyObj('AdminUserService', ['search', 'createAdmin', 'changeRole', 'deleteUser', 'details']);
    service.search.and.returnValue(of({ content: [ME, STUDENT], page: 0, size: 20, totalElements: 2, totalPages: 1 }));
    await TestBed.configureTestingModule({
      imports: [UsersComponent],
      providers: [provideRouter([]), provideHttpClient(), { provide: AdminUserService, useValue: service }]
    }).compileComponents();
    fixture = TestBed.createComponent(UsersComponent);
    fixture.detectChanges();
  });

  afterEach(() => localStorage.clear());

  it('does not show the e-mail column in the list (LGPD)', () => {
    const headers = Array.from(fixture.nativeElement.querySelectorAll('thead th') as NodeListOf<HTMLElement>).map((th) => th.textContent?.trim());
    expect(headers).not.toContain('E-mail');
  });

  it('shows the profile and test history of a user, with a link to each result', () => {
    const detail: AdminUserDetail = {
      ...STUDENT, email: 'joao@teste.com', termsVersion: '1.2', termsAcceptedAt: '2026-11-01T10:00:00',
      results: [{ id: 42, createdAt: '2026-11-05T14:30:00', topCategories: ['Artístico', 'Social'], interestAreas: ['Educação'] }]
    };
    service.details.and.returnValue(of(detail));
    const el: HTMLElement = fixture.nativeElement;
    const button = Array.from(el.querySelectorAll('tbody tr')[1].querySelectorAll('button')).find((b) => b.textContent?.includes('Ver dados'))!;
    button.click();
    fixture.detectChanges();

    expect(service.details).toHaveBeenCalledWith(2);
    const dialog = el.querySelector('[role="dialog"]')!;
    expect(dialog.textContent).toContain('joao@teste.com');
    expect(dialog.textContent).toContain('versão 1.2');
    expect(dialog.textContent).toContain('Testes realizados (1)');
    expect(dialog.textContent).toContain('Perfil Artístico, Social · Educação');
    expect(dialog.querySelector('a[href="/teste-vocacional/resultado/42"]')).toBeTruthy();

    fixture.componentInstance.closeDetails();
    fixture.detectChanges();
    expect(el.querySelector('[role="dialog"]')).toBeNull();
  });

  it('shows an error when the details cannot be loaded', () => {
    service.details.and.returnValue(throwError(() => new HttpErrorResponse({ status: 404, error: { message: 'Usuário não encontrado' } })));
    fixture.componentInstance.openDetails(STUDENT);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('[role="dialog"] [role="alert"]')).toBeTruthy();
  });

  it('blocks changing your own role and offers it for the others', () => {
    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows[0].textContent).toContain('Sua conta: use Meus dados');
    expect(rows[0].textContent).not.toContain('Excluir');
    expect(rows[1].textContent).toContain('Tornar administrador');
    expect(rows[1].textContent).toContain('Excluir');
  });

  it('promotes after confirmation', () => {
    service.changeRole.and.returnValue(of({ ...STUDENT, role: 'ADMIN' }));
    const component = fixture.componentInstance;
    component.askRoleChange(STUDENT);
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).querySelector('#role-password')).toBeTruthy();

    component.confirmRoleChange();
    expect(service.changeRole).not.toHaveBeenCalled();

    component.actionPassword.set('Admin12345');
    component.confirmRoleChange();

    expect(service.changeRole).toHaveBeenCalledWith(2, 'ADMIN', 'Admin12345');
    expect(component.notice()).toContain('administrador');
    expect(component.confirming()).toBeNull();
    expect(component.actionPassword()).toBe('');
  });

  it('deletes another account after confirmation', () => {
    service.deleteUser.and.returnValue(of(undefined));
    fixture.componentInstance.askDelete(STUDENT);
    fixture.componentInstance.actionPassword.set('Admin12345');
    fixture.componentInstance.confirmDelete();

    expect(service.deleteUser).toHaveBeenCalledWith(2, 'Admin12345');
    expect(fixture.componentInstance.notice()).toContain('João');
  });

  it('validates the new admin form before sending', () => {
    const component = fixture.componentInstance;
    component.openCreate();
    component.form.setValue({ name: 'Nova', email: 'nova@teste.com', password: 'fraca' });
    component.saveAdmin();
    expect(service.createAdmin).not.toHaveBeenCalled();

    service.createAdmin.and.returnValue(of({ ...STUDENT, id: 3, name: 'Nova Admin', role: 'ADMIN' }));
    component.form.setValue({ name: 'Nova', email: 'nova@teste.com', password: 'Admin2026x' });
    component.saveAdmin();
    expect(service.createAdmin).toHaveBeenCalledWith({ name: 'Nova', email: 'nova@teste.com', password: 'Admin2026x' });
  });

  const fail = () => throwError(() => new HttpErrorResponse({ status: 500 }));

  it('creates a new admin with the name trimmed and shows a notice', () => {
    const component = fixture.componentInstance;
    service.createAdmin.and.returnValue(of({ ...STUDENT, id: 3, name: 'Nova Admin', role: 'ADMIN' }));
    component.openCreate();
    component.form.setValue({ name: ' Nova Admin ', email: 'nova@teste.com', password: 'Senha@2026' });

    component.saveAdmin();

    expect(service.createAdmin).toHaveBeenCalledWith({ name: 'Nova Admin', email: 'nova@teste.com', password: 'Senha@2026' });
    expect(component.creating()).toBeFalse();
    expect(component.notice()).toContain('Nova Admin');
  });

  it('keeps the form open with an error when creating fails', () => {
    const component = fixture.componentInstance;
    service.createAdmin.and.returnValue(fail());
    component.openCreate();
    component.form.setValue({ name: 'Nova', email: 'nova@teste.com', password: 'Senha@2026' });

    component.saveAdmin();

    expect(component.formError()).toBeTruthy();
    expect(component.saving()).toBeFalse();
    expect(component.creating()).toBeTrue();
  });

  it('shows errors when loading, changing the role or deleting fails', () => {
    const component = fixture.componentInstance;
    service.search.and.returnValue(fail());
    component.load(0);
    expect(component.error()).toContain('usuários');

    // erro de senha ou da API: o modal continua aberto e mostra o erro nele
    service.changeRole.and.returnValue(fail());
    component.askRoleChange(STUDENT);
    component.actionPassword.set('errada');
    component.confirmRoleChange();
    expect(component.actionError()).toContain('perfil');
    expect(component.confirming()).toEqual(STUDENT);
    expect(component.actionBusy()).toBeFalse();

    service.deleteUser.and.returnValue(fail());
    component.askDelete(STUDENT);
    expect(component.actionError()).toBeNull();
    component.actionPassword.set('errada');
    component.confirmDelete();
    expect(component.actionError()).toContain('excluir');
    expect(component.deleting()).toEqual(STUDENT);

    component.closeAction();
    expect(component.deleting()).toBeNull();
  });

  it('ignores confirmations without a selected user and clears the filters', () => {
    const component = fixture.componentInstance;
    component.confirmRoleChange();
    component.confirmDelete();
    expect(service.changeRole).not.toHaveBeenCalled();
    expect(service.deleteUser).not.toHaveBeenCalled();

    component.search = 'ana';
    component.role = 'ADMIN';
    service.search.calls.reset();
    component.clearFilters();
    expect(service.search).toHaveBeenCalledWith(jasmine.objectContaining({ search: '', role: '', page: 0 }));
  });

  it('filters by sign-up period from the calendar and clears it with the other filters', () => {
    const component = fixture.componentInstance;
    service.search.calls.reset();
    component.selectRange({ from: '2026-11-01', to: '2026-11-20' });
    expect(service.search).toHaveBeenCalledWith(jasmine.objectContaining({ from: '2026-11-01', to: '2026-11-20', page: 0 }));

    component.clearFilters();
    expect(component.range()).toEqual({ from: null, to: null });
    expect(service.search).toHaveBeenCalledWith(jasmine.objectContaining({ from: null, to: null, page: 0 }));
  });
});
