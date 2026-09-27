import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { TopBarComponent } from '../../../shared/components/top-bar/top-bar.component';
import { LEGAL_EFFECTIVE_DATE, LEGAL_VERSION } from '../../../core/models/auth.model';
import { environment } from '../../../../environments/environment';

/** Política de Privacidade — específica dos dados que a plataforma realmente coleta (LGPD). */
@Component({
  selector: 'app-privacy-policy',
  standalone: true,
  imports: [RouterLink, TopBarComponent],
  templateUrl: './privacy-policy.component.html'
})
export class PrivacyPolicyComponent {
  readonly version = LEGAL_VERSION;
  readonly effectiveDate = LEGAL_EFFECTIVE_DATE;
  readonly contactEmail = environment.privacyContactEmail;
}
