import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { ResultSummary } from '../../../core/models/auth.model';
import { getErrorMessage } from '../../../core/services/api-error';
import { UserService } from '../../../core/services/user.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';

/** Histórico de testes do usuário (RF08). */
@Component({
  selector: 'app-my-results',
  standalone: true,
  imports: [DatePipe, RouterLink, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './my-results.component.html'
})
export class MyResultsComponent implements OnInit {
  private readonly userService = inject(UserService);

  readonly results = signal<ResultSummary[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.userService.getMyResults().subscribe({
      next: (results) => {
        this.results.set(results);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível carregar seus resultados.'));
        this.loading.set(false);
      }
    });
  }
}
