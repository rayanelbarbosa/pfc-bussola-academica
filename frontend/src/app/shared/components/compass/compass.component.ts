import { ChangeDetectionStrategy, Component } from '@angular/core';

/** Logo da Bússola Acadêmica (ícone pequeno de bússola). */
@Component({
  selector: 'app-compass-mark',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'inline-flex shrink-0', 'aria-hidden': 'true' },
  template: `<svg viewBox="0 0 32 32" class="size-full">
    <circle cx="16" cy="16" r="14" fill="none" stroke="currentColor" stroke-width="1.5" />
    <path d="M20 8 18 18 8 24 14 14Z" fill="none" stroke="currentColor" stroke-width="1.5" />
  </svg>`
})
export class CompassMarkComponent {}

/** Rosa dos ventos com a agulha balançando (ilustração da página inicial e das páginas de erro). */
@Component({
  selector: 'app-compass-rose',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'inline-flex shrink-0', 'aria-hidden': 'true' },
  template: `<svg viewBox="0 0 240 240" class="size-full">
    <circle cx="120" cy="120" r="96" fill="var(--background)" stroke="var(--border)" stroke-width="1.5" />
    <circle cx="120" cy="120" r="78" fill="none" stroke="var(--border)" />
    @for (tick of ticks; track tick) {
      <line x1="120" y1="30" x2="120" [attr.y2]="tick % 6 === 0 ? 43 : 36" stroke="var(--muted-foreground)"
        stroke-width="1" [attr.transform]="'rotate(' + tick * 15 + ' 120 120)'" />
    }
    <text x="120" y="18" text-anchor="middle" font-size="12" fill="var(--muted-foreground)">N</text>
    <text x="227" y="124" text-anchor="middle" font-size="12" fill="var(--muted-foreground)">L</text>
    <text x="120" y="235" text-anchor="middle" font-size="12" fill="var(--muted-foreground)">S</text>
    <text x="12" y="124" text-anchor="middle" font-size="12" fill="var(--muted-foreground)">O</text>
    <g class="compass-spin">
      <path d="M120 58 132 120 108 120Z" fill="var(--primary)" />
      <path d="M120 182 132 120 108 120Z" fill="var(--border)" />
      <circle cx="120" cy="120" r="5" fill="var(--background)" stroke="var(--primary)" stroke-width="2" />
    </g>
  </svg>`
})
export class CompassRoseComponent {
  readonly ticks = Array.from({ length: 24 }, (_, i) => i);
}
