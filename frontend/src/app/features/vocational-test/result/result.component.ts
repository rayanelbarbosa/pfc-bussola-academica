import { ChangeDetectionStrategy, Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { Video } from '../../../core/models/video.model';
import { VocationalTestResult } from '../../../core/models/vocational-test.model';
import { getCategoryInfo, riasecClass } from '../../../core/models/riasec-category';
import { getErrorMessage } from '../../../core/services/api-error';
import { VideoService } from '../../../core/services/video.service';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { RadarChartComponent } from '../../../shared/components/radar-chart/radar-chart.component';

type VideoState = { status: 'loading' } | { status: 'ok'; videos: Video[] } | { status: 'error' };

/**
 * Resultado do teste (RF07, RF08, RF09): radar dos escores, explicação do perfil principal,
 * carreiras mais compatíveis por área escolhida e vídeos do YouTube de cada carreira.
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

  readonly categoryInfo = getCategoryInfo;
  readonly riasecClass = riasecClass;

  readonly radarPoints = computed(() =>
    (this.result()?.scores ?? []).map((score) => ({ label: score.label, value: score.score }))
  );
  readonly hasTie = computed(() => (this.result()?.topCategories.length ?? 0) > 1);

  ngOnInit(): void {
    this.testService.getResult(Number(this.id())).subscribe({
      next: (result) => {
        this.result.set(result);
        this.loading.set(false);
        result.careerRecommendations.forEach((group) => group.careers.forEach((career) => this.loadVideos(career.name)));
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível carregar o resultado.'));
        this.loading.set(false);
      }
    });
  }

  letter(category: string): string {
    return getCategoryInfo(category)?.letter ?? '?';
  }

  /**
   * Busca os vídeos de cada carreira (o back-end guarda as buscas em cache por 24 h, então cada carreira
   * consome a cota da API do YouTube no máximo uma vez por dia).
   */
  loadVideos(career: string): void {
    const current = this.videos()[career];
    if (current && current.status !== 'error') {
      return;
    }
    this.setVideos(career, { status: 'loading' });
    this.videoService.searchByCareer(career).subscribe({
      next: (videos) => this.setVideos(career, { status: 'ok', videos }),
      error: () => this.setVideos(career, { status: 'error' })
    });
  }

  private setVideos(career: string, state: VideoState): void {
    this.videos.update((all) => ({ ...all, [career]: state }));
  }
}
