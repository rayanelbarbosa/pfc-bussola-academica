import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { Option } from '../../../core/models/admission-method.model';
import { MAX_INTEREST_AREAS } from '../../../core/models/vocational-test.model';
import { getErrorMessage } from '../../../core/services/api-error';
import { QuizDraftService } from '../../../core/services/quiz-draft.service';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';

/** Etapa 1 do teste (RF06): escolha de 1 a 3 áreas de interesse. */
@Component({
  selector: 'app-areas',
  standalone: true,
  imports: [IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './areas.component.html'
})
export class AreasComponent implements OnInit {
  private readonly testService = inject(VocationalTestService);
  private readonly draft = inject(QuizDraftService);
  private readonly router = inject(Router);

  readonly max = MAX_INTEREST_AREAS;
  readonly selected = this.draft.areas;
  readonly areas = signal<Option[]>([]);
  readonly loading = signal(true);
  readonly loadError = signal<string | null>(null);
  readonly warning = signal<string | null>(null);

  ngOnInit(): void {
    this.testService.getInterestAreas().subscribe({
      next: (areas) => {
        this.areas.set(areas);
        this.loading.set(false);
      },
      error: (err) => {
        this.loadError.set(getErrorMessage(err, 'Não foi possível carregar as áreas.'));
        this.loading.set(false);
      }
    });
  }

  toggle(area: string): void {
    this.warning.set(this.draft.toggleArea(area));
  }

  start(): void {
    if (this.selected().length > 0) {
      this.router.navigate(['/teste-vocacional/perguntas']);
    }
  }
}
