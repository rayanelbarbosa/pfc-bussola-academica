import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { LEGAL_EFFECTIVE_DATE, LEGAL_VERSION } from '../../../core/models/auth.model';
import { environment } from '../../../../environments/environment';

/** Termo de Uso (Termo de Aceite) da plataforma — acessível a qualquer momento, com ou sem login. */
@Component({
  selector: 'app-terms-of-use',
  standalone: true,
  imports: [RouterLink, TopBarComponent],
  templateUrl: './terms-of-use.component.html'
})
export class TermsOfUseComponent {
  readonly version = LEGAL_VERSION;
  readonly effectiveDate = LEGAL_EFFECTIVE_DATE;
  readonly contactEmail = environment.privacyContactEmail;
}
