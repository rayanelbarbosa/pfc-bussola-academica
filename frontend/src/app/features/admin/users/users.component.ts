import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Page } from '../../../core/models/audit.model';
import { Role } from '../../../core/models/auth.model';
import { AdminUser, AdminUserDetail, AdminUserService } from '../../../core/services/admin-user.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { AuthService } from '../../../core/services/auth.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { PASSWORD_PATTERN } from '../../auth/register/register.component';
import { PasswordToggleComponent } from '../../../shared/components/password-toggle/password-toggle.component';

/**
 * Gestão de usuários (RF12): lista paginada com busca e filtro, cadastro de administrador e
 * promoção/rebaixamento e exclusão de conta, sempre com confirmação. A própria linha fica bloqueada.
 */
@Component({
  selector: 'app-users',
  standalone: true,
  imports: [DatePipe, FormsModule, ReactiveFormsModule, RouterLink, IconComponent, PasswordToggleComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {
  private readonly service = inject(AdminUserService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly pageSize = 20;
  readonly roleLabels: Record<string, string> = { STUDENT: 'Estudante', ADMIN: 'Administrador' };
  readonly myId = () => this.auth.user()?.id;

  search = '';
  role = '';
  readonly page = signal<Page<AdminUser> | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly notice = signal<string | null>(null);
  readonly confirming = signal<AdminUser | null>(null);
  readonly deleting = signal<AdminUser | null>(null);
  /** Painel "Ver dados": cadastro e histórico de testes do usuário escolhido. */
  readonly viewing = signal<AdminUser | null>(null);
  readonly detail = signal<AdminUserDetail | null>(null);
  readonly detailError = signal<string | null>(null);

  readonly creating = signal(false);
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);
  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
    password: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]]
  });

  ngOnInit(): void {
    this.load(0);
  }

  load(pageNumber: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.search({ search: this.search, role: this.role, page: pageNumber, size: this.pageSize }).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível carregar os usuários.'));
        this.loading.set(false);
      }
    });
  }

  clearFilters(): void {
    this.search = '';
    this.role = '';
    this.load(0);
  }

  targetRole(user: AdminUser): Role {
    return user.role === 'ADMIN' ? 'STUDENT' : 'ADMIN';
  }

  confirmRoleChange(): void {
    const user = this.confirming();
    if (!user) return;
    this.confirming.set(null);
    this.service.changeRole(user.id, this.targetRole(user)).subscribe({
      next: (saved) => {
        this.notice.set(`${saved.name} agora é ${this.roleLabels[saved.role].toLowerCase()}.`);
        this.load(this.page()?.page ?? 0);
      },
      error: (err) => this.error.set(getErrorMessage(err, 'Não foi possível alterar o perfil.'))
    });
  }

  /** Abre o painel e busca o cadastro e o histórico (a consulta fica registrada na auditoria). */
  openDetails(user: AdminUser): void {
    this.viewing.set(user);
    this.detail.set(null);
    this.detailError.set(null);
    this.service.details(user.id).subscribe({
      next: (detail) => this.detail.set(detail),
      error: (err) => this.detailError.set(getErrorMessage(err, 'Não foi possível carregar os dados do usuário.'))
    });
  }

  closeDetails(): void {
    this.viewing.set(null);
    this.detail.set(null);
  }

  confirmDelete(): void {
    const user = this.deleting();
    if (!user) return;
    this.deleting.set(null);
    this.service.deleteUser(user.id).subscribe({
      next: () => {
        this.notice.set(`A conta de ${user.email} foi excluída.`);
        this.load(this.page()?.page ?? 0);
      },
      error: (err) => this.error.set(getErrorMessage(err, 'Não foi possível excluir a conta.'))
    });
  }

  openCreate(): void {
    this.form.reset();
    this.formError.set(null);
    this.creating.set(true);
  }

  saveAdmin(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.formError.set(null);
    const { name, email, password } = this.form.getRawValue();
    this.service.createAdmin({ name: name.trim(), email: email.trim(), password }).subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.creating.set(false);
        this.notice.set(`Administrador ${saved.email} cadastrado.`);
        this.load(0);
      },
      error: (err) => {
        this.saving.set(false);
        this.formError.set(getErrorMessage(err, 'Não foi possível cadastrar o administrador.'));
      }
    });
  }

  isInvalid(field: 'name' | 'email' | 'password'): boolean {
    const control = this.form.controls[field];
    return control.invalid && control.touched;
  }
}
