import { ChangeDetectionStrategy, Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { Video } from '../../../core/models/video.model';
import { VocationalTestResult } from '../../../core/models/vocational-test.model';
import { getCategoryInfo, riasecClass, riasecColor } from '../../../core/models/riasec-category';
import { getErrorMessage } from '../../../core/services/api-error';
import { VideoService } from '../../../core/services/video.service';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { RadarChartComponent } from '../../../shared/components/radar-chart/radar-chart.component';

type VideoState = { status: 'loading' } | { status: 'ok'; videos: Video[] } | { status: 'error' };

/**
 * Resultado do teste (RF07, RF08, RF09): explicação do perfil principal, radar dos escores,
 * as 4 carreiras mais compatíveis entre as áreas escolhidas e vídeos do YouTube de cada carreira.
 */
@Component({
  selector: 'app-result',
  standalone: true,
  imports: [DatePipe, RouterLink, IconComponent, RadarChartComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './result.component.html'
})
export class ResultComponent implements OnInit {
  private readonly testService = inject(VocationalTestService);
  private readonly videoService = inject(VideoService);

  /** Id do resultado, vindo da rota (/teste-vocacional/resultado/:id). */
  readonly id = input.required<string>();

  readonly result = signal<VocationalTestResult | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly videos = signal<Partial<Record<string, VideoState>>>({});
  /** Aviso de área que combina pouco: true = o estudante escolheu ver as carreiras que mais combinam com ele. */
  readonly showSuggested = signal(false);
  /** Carreiras com o "Mais sobre a carreira" aberto. */
  readonly openDetails = signal<ReadonlySet<string>>(new Set());

  readonly categoryInfo = getCategoryInfo;
  readonly riasecClass = riasecClass;
  readonly riasecColor = riasecColor;

  readonly radarPoints = computed(() =>
    (this.result()?.scores ?? []).map((score) => ({ label: score.label, value: score.score, color: riasecColor(score.category) }))
  );
  /** "Investigativo" ou, com empate, "Investigativo e Social". */
  readonly profileName = computed(() => {
    const names = (this.result()?.topCategories ?? []).map((top) => top.label);
    return names.length > 1 ? names.slice(0, -1).join(', ') + ' e ' + names.at(-1) : names[0] ?? '';
  });

  /** Com mais de uma área: comparação entre elas pela melhor carreira de cada uma (a 1ª é a que mais combina). */
  readonly areaComparison = computed(() => {
    const matches = this.result()?.areaMatches ?? [];
    return matches.length < 2 ? null : matches;
  });

  /** Há carreiras de outras áreas que combinam bem mais com o perfil do que as das áreas escolhidas. */
  readonly hasSuggestion = computed(() => (this.result()?.suggestedCareers ?? []).length > 0);

  /** Lista exibida: as áreas escolhidas (padrão, respeita a escolha) ou as que mais combinam, se ele preferir. */
  readonly shownCareers = computed(() => {
    const r = this.result();
    if (!r) {
      return [];
    }
    return this.showSuggested() && this.hasSuggestion() ? r.suggestedCareers : r.careerRecommendations;
  });

  /** Os 2 tipos de maior escore do estudante: o back-end usa para ordenar os vídeos (ranking), não para buscar. */
  readonly videoProfile = computed(() =>
    [...(this.result()?.scores ?? [])]
      .sort((a, b) => b.score - a.score)
      .slice(0, 2)
      .map((score) => score.category)
  );

  ngOnInit(): void {
    this.testService.getResult(Number(this.id())).subscribe({
      next: (result) => {
        this.result.set(result);
        this.loading.set(false);
        result.careerRecommendations.forEach((career) => this.loadVideos(career.name));
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível carregar o resultado.'));
        this.loading.set(false);
      }
    });
  }

  /** Escore do estudante (2 a 10) num tipo RIASEC: usado para mostrar a conta da compatibilidade. */
  scoreOf(category: string): number {
    return this.result()?.scores.find((score) => score.category === category)?.score ?? 0;
  }

  letter(category: string): string {
    return getCategoryInfo(category)?.letter ?? '?';
  }

  /** Troca entre as carreiras das áreas escolhidas e as que mais combinam; busca os vídeos das que aparecerem. */
  setShowSuggested(show: boolean): void {
    this.showSuggested.set(show);
    this.shownCareers().forEach((career) => this.loadVideos(career.name));
  }

  toggleDetails(career: string): void {
    this.openDetails.update((open) => {
      const next = new Set(open);
      if (!next.delete(career)) {
        next.add(career);
      }
      return next;
    });
  }

  /**
   * Busca os vídeos de cada carreira. O back-end guarda os candidatos em cache por 24 h (cada carreira consome a
   * cota do YouTube no máximo uma vez por dia) e reordena os vídeos pelo perfil do estudante.
   */
  loadVideos(career: string): void {
    const current = this.videos()[career];
    if (current && current.status !== 'error') {
      return;
    }
    this.setVideos(career, { status: 'loading' });
    this.videoService.searchByCareer(career, this.videoProfile()).subscribe({
      next: (videos) => this.setVideos(career, { status: 'ok', videos }),
      error: () => this.setVideos(career, { status: 'error' })
    });
  }

  private setVideos(career: string, state: VideoState): void {
    this.videos.update((all) => ({ ...all, [career]: state }));
  }
}
