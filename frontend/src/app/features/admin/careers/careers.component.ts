import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { AbstractControl, FormBuilder, FormsModule, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';

import { Option } from '../../../core/models/admission-method.model';
import { Page } from '../../../core/models/audit.model';
import { Career } from '../../../core/models/career.model';
import { riasecClass, riasecColor } from '../../../core/models/riasec-category';
import { getErrorMessage } from '../../../core/services/api-error';
import { CareerService } from '../../../core/services/career.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';

/** Os tipos do código precisam ser diferentes entre si (mesma regra do back-end); o 3º tipo é opcional. */
export function differentTypes(group: AbstractControl): ValidationErrors | null {
  const primary = group.get('primaryCategory')?.value;
  const secondary = group.get('secondaryCategory')?.value;
  const tertiary = group.get('tertiaryCategory')?.value;
  if (primary && secondary && primary === secondary) {
    return { sameType: true };
  }
  if (tertiary && (tertiary === primary || tertiary === secondary)) {
    return { sameTertiary: true };
  }
  return null;
}

/**
 * Gestão das carreiras pelo administrador: consulta com filtros, inclusão, alteração e ativação/desativação.
 * A carreira nova entra na recomendação do teste e na busca de vídeos sem precisar de deploy.
 */
/** Linhas preenchidas do campo "Dia a dia" (uma tarefa por linha). */
function taskLines(text: string): string[] {
  return text.split('\n').map((line) => line.trim()).filter((line) => line.length > 0);
}

/** Até 3 tarefas, cada uma com até 150 caracteres e sem "|" (separador usado no banco). */
function maxTasks(control: AbstractControl): ValidationErrors | null {
  const lines = taskLines(String(control.value ?? ''));
  return lines.length > 3 || lines.some((line) => line.length > 150 || line.includes('|')) ? { tasks: true } : null;
}

@Component({
  selector: 'app-careers',
  standalone: true,
  imports: [DatePipe, FormsModule, ReactiveFormsModule, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './careers.component.html'
})
export class CareersComponent implements OnInit {
  private readonly service = inject(CareerService);
  private readonly fb = inject(FormBuilder);

  readonly pageSize = 10;
  readonly riasecClass = riasecClass;
  readonly riasecColor = riasecColor;
  readonly areas = signal<Option[]>([]);
  readonly categories = signal<Option[]>([]);
  readonly page = signal<Page<Career> | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly notice = signal<string | null>(null);

  filterName = '';
  filterArea = '';
  filterActive = '';

  /** null = fechado; 0 = inclusão; id = alteração */
  readonly editingId = signal<number | null>(null);
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);
  readonly confirming = signal<Career | null>(null);

  readonly form = this.fb.nonNullable.group(
    {
      name: ['', [Validators.required, Validators.maxLength(100)]],
      area: ['', Validators.required],
      primaryCategory: ['', Validators.required],
      secondaryCategory: ['', Validators.required],
      tertiaryCategory: [''],
      description: ['', [Validators.required, Validators.maxLength(300)]],
      // uma tarefa por linha (até 3); vira lista no envio
      dailyTasks: ['', [maxTasks]],
      active: [true]
    },
    { validators: differentTypes }
  );

  private readonly formValue = signal(this.form.getRawValue());
  /** Prévia do código RIASEC (ex.: "IR") enquanto o administrador escolhe os tipos. */
  readonly codePreview = computed(() => {
    const value = this.formValue();
    return (value.primaryCategory?.charAt(0) ?? '') + (value.secondaryCategory?.charAt(0) ?? '') + (value.tertiaryCategory?.charAt(0) ?? '');
  });

  constructor() {
    this.form.valueChanges.subscribe(() => this.formValue.set(this.form.getRawValue()));
  }

  ngOnInit(): void {
    this.service.areas().subscribe({ next: (areas) => this.areas.set(areas) });
    this.service.categories().subscribe({ next: (categories) => this.categories.set(categories) });
    this.load(0);
  }

  load(pageNumber: number): void {
    this.loading.set(true);
    this.error.set(null);
    this.service
      .search({ name: this.filterName, area: this.filterArea, active: this.filterActive, page: pageNumber, size: this.pageSize })
      .subscribe({
        next: (page) => {
          this.page.set(page);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set(getErrorMessage(err, 'Não foi possível carregar as carreiras.'));
          this.loading.set(false);
        }
      });
  }

  clearFilters(): void {
    this.filterName = '';
    this.filterArea = '';
    this.filterActive = '';
    this.load(0);
  }

  openCreate(): void {
    this.form.reset({ active: true });
    this.formError.set(null);
    this.editingId.set(0);
  }

  openEdit(career: Career): void {
    this.form.reset({
      name: career.name,
      area: career.area,
      primaryCategory: career.primaryCategory,
      secondaryCategory: career.secondaryCategory,
      tertiaryCategory: career.tertiaryCategory ?? '',
      description: career.description,
      dailyTasks: (career.dailyTasks ?? []).join('\n'),
      active: career.active
    });
    this.formError.set(null);
    this.editingId.set(career.id);
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
    const request = { ...value, tertiaryCategory: value.tertiaryCategory || null, dailyTasks: taskLines(value.dailyTasks) };
    const id = this.editingId();
    const call = id ? this.service.update(id, request) : this.service.create(request);
    this.saving.set(true);
    this.formError.set(null);
    call.subscribe({
      next: (saved) => {
        this.saving.set(false);
        this.editingId.set(null);
        this.notice.set(id ? `"${saved.name}" foi alterada.` : `"${saved.name}" foi incluída e já entra na recomendação do teste.`);
        this.load(this.page()?.page ?? 0);
      },
      error: (err) => {
        this.saving.set(false);
        this.formError.set(getErrorMessage(err, 'Não foi possível salvar. Confira os campos e tente novamente.'));
      }
    });
  }

  confirmStatusChange(): void {
    const career = this.confirming();
    if (!career) return;
    this.confirming.set(null);
    this.service.changeStatus(career.id, !career.active).subscribe({
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

  get sameType(): boolean {
    return this.form.hasError('sameType') && this.form.controls.secondaryCategory.touched;
  }

  get sameTertiary(): boolean {
    return this.form.hasError('sameTertiary');
  }

  length(field: keyof typeof this.form.controls): number {
    return String(this.form.controls[field].value ?? '').length;
  }
}
