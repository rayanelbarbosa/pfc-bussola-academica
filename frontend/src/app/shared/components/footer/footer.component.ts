import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Rodapé com os links institucionais (inclui o Termo de Uso e a Política de Privacidade, LGPD). */
@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <footer class="mt-auto border-t border-border bg-background">
      <div class="container-page flex flex-col gap-4 py-6 md:flex-row md:items-center md:justify-between">
        <span class="font-semibold">Bússola Acadêmica</span>
        <nav aria-label="Rodapé" class="flex flex-wrap gap-x-5 gap-y-3 text-sm text-muted-foreground">
          <a routerLink="/formas-de-ingresso" class="hover:text-primary">Formas de ingresso</a>
          <a routerLink="/como-funciona" class="hover:text-primary">Como funciona o teste</a>
          <a routerLink="/teste-vocacional" class="hover:text-primary">Teste vocacional</a>
          <a routerLink="/termos-de-uso" class="hover:text-primary">Termo de Uso</a>
          <a routerLink="/politica-de-privacidade" class="hover:text-primary">Política de Privacidade</a>
        </nav>
      </div>
    </footer>
  `
})
export class FooterComponent {}
