import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

import { ResultSummary, User } from '../../../core/models/auth.model';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { IconComponent } from '../../../shared/components/icon/icon.component';

/** Meus dados: acesso, exportação em JSON e exclusão da conta (direitos do titular, LGPD art. 18). */
@Component({
  selector: 'app-my-data',
  standalone: true,
  imports: [DatePipe, RouterLink, IconComponent],
  templateUrl: './my-data.component.html'
})
export class MyDataComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly user = signal<User | null>(null);
  readonly results = signal<ResultSummary[]>([]);
  readonly error = signal<string | null>(null);
  readonly confirmingDeletion = signal(false);
  readonly deleting = signal(false);
  readonly exporting = signal(false);

  readonly roleLabels: Record<string, string> = { STUDENT: 'Estudante', ADMIN: 'Administrador(a)' };

  ngOnInit(): void {
    this.userService.getMe().subscribe({
      next: (user) => this.user.set(user),
      error: (err) => this.error.set(getErrorMessage(err, 'Não foi possível carregar seus dados.'))
    });
    this.userService.getMyResults().subscribe({
      next: (results) => this.results.set(results),
      error: () => this.results.set([])
    });
  }

  /** Baixa o JSON gerado pela API como arquivo. */
  downloadMyData(): void {
    this.exporting.set(true);
    this.error.set(null);
    this.userService.exportMyData().subscribe({
      next: (file) => {
        const url = URL.createObjectURL(file);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'meus-dados-bussola-academica.json';
        link.click();
        URL.revokeObjectURL(url);
        this.exporting.set(false);
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível exportar seus dados. Tente novamente.'));
        this.exporting.set(false);
      }
    });
  }

  deleteAccount(): void {
    this.deleting.set(true);
    this.userService.deleteMyAccount().subscribe({
      next: () => {
        this.auth.clear();
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível excluir a conta. Tente novamente.'));
        this.deleting.set(false);
      }
    });
  }
}
