import { ChangeDetectionStrategy, Component, input, signal } from '@angular/core';

/**
 * Botão de olho que mostra ou esconde a senha do campo informado.
 * Uso: envolver o input em um "relative", dar a ele uma referência (#campo) e a classe pr-11:
 * <div class="relative"><input #senha type="password" class="field-input pr-11" /><app-password-toggle [target]="senha" /></div>
 */
@Component({
  selector: 'app-password-toggle',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'absolute inset-y-0 right-0 flex items-center pr-1' },
  template: `
    <button type="button" class="grid size-9 place-items-center rounded-md text-muted-foreground hover:bg-muted hover:text-foreground"
      [attr.aria-label]="visible() ? 'Esconder senha' : 'Mostrar senha'" [attr.aria-pressed]="visible()" (click)="toggle()">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
        class="size-5" aria-hidden="true">
        @if (visible()) {
          <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68" />
          <path d="M6.61 6.61A13.53 13.53 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61" />
          <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" /><path d="m2 2 20 20" />
        } @else {
          <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" /><circle cx="12" cy="12" r="3" />
        }
      </svg>
    </button>
  `
})
export class PasswordToggleComponent {
  readonly target = input.required<HTMLInputElement>();
  readonly visible = signal(false);

  toggle(): void {
    this.visible.update((value) => !value);
    this.target().type = this.visible() ? 'text' : 'password';
  }
}
