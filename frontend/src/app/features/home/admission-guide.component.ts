import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';

import { AdmissionMethod } from '../../core/models/admission-method.model';
import { AdmissionMethodService } from '../../core/services/admission-method.service';
import { getErrorMessage } from '../../core/services/api-error';
import { IconComponent } from '../../shared/components/icon/icon.component';

/** Formas de ingresso (RF01): lista expansível com filtro por tipo, em página própria. */
@Component({
  selector: 'app-admission-guide',
  standalone: true,
  imports: [DatePipe, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admission-guide.component.html'
})
export class AdmissionGuideComponent implements OnInit {
  private readonly service = inject(AdmissionMethodService);

  readonly methods = signal<AdmissionMethod[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly selectedType = signal<string>('');

  /** Tipos presentes na lista, na ordem em que aparecem (para os chips de filtro). */
  readonly types = computed(() => {
    const seen = new Map<string, string>();
    this.methods().forEach((method) => seen.set(method.type, method.typeLabel));
    return [...seen.entries()].map(([value, label]) => ({ value, label }));
  });
  readonly filtered = computed(() =>
    this.methods().filter((method) => !this.selectedType() || method.type === this.selectedType())
  );

  /** Ícone de cada tipo de forma de ingresso (só visual). */
  iconFor(type: string): string {
    const icons: Record<string, string> = {
      NATIONAL_EXAM: 'list-checks',
      GOVERNMENT_PROGRAM: 'graduation-cap',
      STUDENT_FINANCING: 'wallet',
      ENTRANCE_EXAM: 'pencil',
      SCHOLARSHIP_PORTAL: 'search'
    };
    return icons[type] ?? 'book-open';
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.listActive().subscribe({
      next: (methods) => {
        this.methods.set(methods);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não conseguimos carregar as formas de ingresso agora.'));
        this.loading.set(false);
      }
    });
  }
}
