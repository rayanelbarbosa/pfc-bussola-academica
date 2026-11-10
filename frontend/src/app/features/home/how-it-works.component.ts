import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { RIASEC_INFO, riasecClass } from '../../core/models/riasec-category';
import { IconComponent } from '../../shared/components/icon/icon.component';

/** Explica o modelo RIASEC, o hexágono de Holland e os passos do teste vocacional, em página própria. */
@Component({
  selector: 'app-how-it-works',
  standalone: true,
  imports: [RouterLink, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './how-it-works.component.html'
})
export class HowItWorksComponent {
  readonly riasec = RIASEC_INFO;
  readonly riasecClass = riasecClass;
}
