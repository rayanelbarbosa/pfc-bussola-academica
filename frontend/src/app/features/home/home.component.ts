import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { AdmissionMethod } from '../../core/models/admission-method.model';
import { RIASEC_INFO, riasecClass } from '../../core/models/riasec-category';
import { AdmissionMethodService } from '../../core/services/admission-method.service';
import { getErrorMessage } from '../../core/services/api-error';
import { CompassRoseComponent } from '../../shared/components/compass/compass.component';
import { IconComponent } from '../../shared/components/icon/icon.component';

/** Página inicial pública (RF01): contexto, formas de ingresso em lista expansível e explicação do teste. */
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [DatePipe, RouterLink, CompassRoseComponent, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './home.component.html'
})
export class HomeComponent implements OnInit {
  private readonly service = inject(AdmissionMethodService);

  readonly riasec = RIASEC_INFO;
  readonly riasecClass = riasecClass;
  readonly contextCards = [
    { icon: 'layers', title: 'Muitas opções, pouca informação', text: 'As formas de ingresso ficam espalhadas em sites diferentes. Comparar os caminhos nem sempre é fácil.' },
    { icon: 'book-open', title: 'O que este site faz', text: 'Explica cada forma de ingresso em um lugar só, com requisitos, custos e etapas.' },
    { icon: 'circle-help', title: 'E qual curso escolher?', text: 'O teste vocacional ajuda a conhecer seus interesses e explorar áreas e carreiras.' }
  ];

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
