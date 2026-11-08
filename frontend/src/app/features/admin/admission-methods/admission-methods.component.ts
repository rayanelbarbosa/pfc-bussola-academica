import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

import { Page } from '../../../core/models/audit.model';
import { AdmissionMethod, Option } from '../../../core/models/admission-method.model';
import { AdmissionMethodService } from '../../../core/services/admission-method.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { IconComponent } from '../../../shared/components/icon/icon.component';

/** Link oficial: precisa começar com http:// ou https:// (mesma regra do back-end). */
export const URL_PATTERN = /^https?:\/\/\S+\.\S+/;

/**
 * Gestão das formas de ingresso pelo administrador (RF11): consulta com filtros, inclusão,
 * alteração e ativação/desativação (não há exclusão).
 */
@Component({
  selector: 'app-admission-methods',
  standalone: true,
  imports: [DatePipe, FormsModule, ReactiveFormsModule, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admission-methods.component.html'
})
export class AdmissionMethodsComponent implements OnInit {
  private readonly service = inject(AdmissionMethodService);
  private readonly fb = inject(FormBuilder);

  readonly pageSize = 10;
  readonly types = signal<Option[]>([]);
  readonly page = signal<Page<AdmissionMethod> | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly notice = signal<string | null>(null);

  filterName = '';
  filterType = '';
  filterActive = '';

  /** null = fechado; 0 = inclusão; id = alteração */
  readonly editingId = signal<number | null>(null);
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);
  readonly confirming = signal<AdmissionMethod | null>(null);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(100)]],
    type: ['', Validators.required],
    summary: ['', [Validators.required, Validators.maxLength(300)]],
    description: ['', [Validators.required, Validators.maxLength(1000)]],
    howItWorks: ['', [Validators.required, Validators.maxLength(1500)]],
    eligibility: ['', [Validators.required, Validators.maxLength(1000)]],
    schedule: ['', [Validators.required, Validators.maxLength(500)]],
    cost: ['', [Validators.required, Validators.maxLength(500)]],
    tips: ['', Validators.maxLength(1000)],
    officialUrl: ['', [Validators.maxLength(300), Validators.pattern(URL_PATTERN)]],
    active: [true]
  });

  ngOnInit(): void {
    this.service.types().subscribe({ next: (types) => this.types.set(types) });
    this.load(0);
  }

  load(pageNumber: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.service
      .search({ name: this.filterName, type: this.filterType, active: this.filterActive, page: pageNumber, size: this.pageSize })
      .subscribe({
        next: (page) => {
          this.page.set(page);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(getErrorMessage(err, 'Não foi possível carregar as formas de ingresso.'));
          this.loading.set(false);
        }
      });
  }

  clearFilters(): void {
    this.filterName = '';
    this.filterType = '';
    this.filterActive = '';
    this.load(0);
  }

  openCreate(): void {
    this.form.reset({ active: true });
    this.formError.set(null);
    this.editingId.set(0);
  }

  openEdit(method: AdmissionMethod): void {
    this.form.reset({
      name: method.name,
      type: method.type,
      summary: method.summary,
      description: method.description,
      howItWorks: method.howItWorks.join('\n'),
      eligibility: method.eligibility,
      schedule: method.schedule,
      cost: method.cost,
      tips: method.tips ?? '',
      officialUrl: method.officialUrl ?? '',
      active: method.active
    });
    this.formError.set(null);
    this.editingId.set(method.id);
  }

  closeForm(): void {
    this.editingId.set(null);
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const request = { ...value, tips: value.tips.trim() ? value.tips : null };
    const id = this.editingId();
    const call = id ? this.service.update(id, request) : this.service.create(request);
    this.saving.set(true);
    this.formError.set(null);
    call.subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.editingId.set(null);
        this.notice.set(id ? `"${saved.name}" foi alterada.` : `"${saved.name}" foi incluída.`);
        this.load(this.page()?.page ?? 0);
      },
      error: (err) => {
        this.saving.set(false);
        this.formError.set(getErrorMessage(err, 'Não foi possível salvar. Confira os campos e tente novamente.'));
      }
    });
  }

  confirmStatusChange(): void {
    const method = this.confirming();
    if (!method) return;
    this.confirming.set(null);
    this.service.changeStatus(method.id, !method.active).subscribe({
      next: (saved) => {
        this.notice.set(`"${saved.name}" foi ${saved.active ? 'reativada' : 'desativada'}.`);
        this.load(this.page()?.page ?? 0);
      },
      error: (err) => this.error.set(getErrorMessage(err, 'Não foi possível alterar o status.'))
    });
  }

  isInvalid(field: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[field];
    return control.invalid && control.touched;
  }

  length(field: keyof typeof this.form.controls): number {
    return String(this.form.controls[field].value ?? '').length;
  }
}
