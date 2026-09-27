import { Component } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { TopBarComponent } from '../../shared/components/top-bar/top-bar.component';

/** Tela inicial (protótipo 02 - Home). */
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [DatePipe, RouterLink, TopBarComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {
  readonly today = new Date();

  readonly infoCards = [
    {
      title: 'Base científica',
      text: 'Baseado no modelo RIASEC (Holland), referência em orientação vocacional.'
    },
    {
      title: 'Cálculo transparente',
      text: 'O escore de cada categoria é a soma direta das suas respostas — sem caixa-preta.'
    },
    {
      title: 'Áreas recomendadas',
      text: 'Veja cursos e áreas ligados ao seu perfil predominante ao final do teste.'
    }
  ];
}
