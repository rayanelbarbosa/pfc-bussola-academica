import { Injectable, inject } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';

/**
 * Eventos marcados no front (#150). Os nomes seguem o padrão do GA4 (snake_case; os recomendados, como page_view,
 * sign_up e login, com o mesmo nome do Google), então dá para ligar o GA4 ou o Google Tag Manager depois sem mudar
 * nenhuma marcação.
 */
export type AnalyticsEvent =
  | 'page_view'
  | 'sign_up'
  | 'login'
  | 'logout'
  | 'password_reset_requested'
  | 'password_reset_completed'
  | 'test_started'
  | 'test_completed'
  | 'full_ranking_toggled'
  | 'suggested_careers_toggled'
  | 'career_details_opened'
  | 'data_exported'
  | 'account_deleted'
  | 'period_filter_applied'
  | 'user_details_viewed'
  | 'audit_integrity_checked';

/** Só valores simples e NUNCA dado pessoal (nome, e-mail, id de usuário): LGPD, minimização. */
export type AnalyticsParams = Record<string, string | number | boolean | null>;

declare global {
  interface Window {
    dataLayer?: Record<string, unknown>[];
  }
}

/**
 * Tagueamento de eventos: cada evento vai para o window.dataLayer (a fila padrão lida pelo GA4/GTM) e aparece no
 * console do navegador (F12 > Console) para conferência. Nada é enviado para fora do site: sem cookies e sem
 * terceiros, então a CSP e os termos não mudam. Para ver tudo de uma vez: digite "dataLayer" no console.
 */
@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly router = inject(Router);
  private started = false;

  /** Começa a registrar as trocas de página (page_view). Chamado uma vez, pelo AppComponent. */
  start(): void {
    if (this.started) return;
    this.started = true;
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => this.track('page_view', { page_path: AnalyticsService.anonymizePath(event.urlAfterRedirects) }));
  }

  track(event: AnalyticsEvent, params: AnalyticsParams = {}): void {
    const entry = { event, ...params, event_time: new Date().toISOString() };
    (window.dataLayer ??= []).push(entry);
    console.info(`%c[evento] ${event}`, 'color:#7c3aed;font-weight:600', params);
  }

  /** Tira da URL o que poderia identificar alguém: números (ids) viram ":id" e a query string (tokens) sai. */
  static anonymizePath(url: string): string {
    return url.split(/[?#]/)[0].replace(/\/\d+(?=\/|$)/g, '/:id');
  }
}
