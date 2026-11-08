import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CompassRoseComponent } from '../../shared/components/compass/compass.component';

/** Páginas de erro: 404 (rota inexistente) e 403 (acesso negado, RF16). Textos vêm do data da rota. */
@Component({
  selector: 'app-error-page',
  standalone: true,
  imports: [RouterLink, CompassRoseComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="container-page flex flex-1 flex-col items-center justify-center gap-4 py-16 text-center">
      <app-compass-rose class="size-20" />
      <span class="text-sm text-muted-foreground">{{ code() }}</span>
      <h1>{{ heading() }}</h1>
      <p class="max-w-md text-muted-foreground">{{ description() }}</p>
      <a routerLink="/" class="btn btn-primary mt-2">Voltar ao início</a>
    </div>
  `
})
export class ErrorPageComponent {
  readonly code = input('404');
  readonly heading = input('Parece que você saiu da rota');
  readonly description = input('A página que você procura não existe ou mudou de endereço.');
}
