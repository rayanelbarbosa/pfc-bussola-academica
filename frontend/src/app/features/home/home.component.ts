import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { RIASEC_INFO } from '../../core/models/riasec-category';
import { CompassRoseComponent } from '../../shared/components/compass/compass.component';
import { IconComponent } from '../../shared/components/icon/icon.component';

/** Página inicial pública: apresenta o site e leva para as formas de ingresso e para o teste vocacional. */
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, CompassRoseComponent, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './home.component.html'
})
export class HomeComponent {
  readonly riasec = RIASEC_INFO;
  readonly contextCards = [
    { icon: 'layers', title: 'Muitas opções, pouca informação', text: 'As formas de ingresso ficam espalhadas em sites diferentes. Comparar os caminhos nem sempre é fácil.' },
    { icon: 'book-open', title: 'O que este site faz', text: 'Explica cada forma de ingresso em um lugar só, com requisitos, custos e etapas.' },
    { icon: 'circle-help', title: 'E qual curso escolher?', text: 'O teste vocacional ajuda a conhecer seus interesses e explorar áreas e carreiras.' }
  ];

  /** Formas de ingresso mais conhecidas, só como atalho visual (a lista completa vem da API na página própria). */
  readonly highlights = ['Enem', 'Sisu', 'Prouni', 'Fies', 'Vestibulares'];
}
