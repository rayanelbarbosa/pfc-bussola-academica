import { Component, computed, input } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { RouterLink } from '@angular/router';

import { parseLegalDocument } from './legal-document';

/** Mostra um texto legal (Termo de Uso ou Política de Privacidade) guardado no banco, já formatado. */
@Component({
  selector: 'app-legal-document',
  standalone: true,
  imports: [NgTemplateOutlet, RouterLink],
  template: `
    <ng-template #inline let-segments>
      @for (s of segments; track $index) {
        @switch (s.kind) {
          @case ('bold') { <strong>{{ s.text }}</strong> }
          @case ('link') {
            @if (s.internal) {
              <a [routerLink]="s.href">{{ s.text }}</a>
            } @else {
              <a [href]="s.href" target="_blank" rel="noopener noreferrer">{{ s.text }}</a>
            }
          }
          @default { {{ s.text }} }
        }
      }
    </ng-template>

    @for (block of blocks(); track $index) {
      @switch (block.kind) {
        @case ('heading') {
          @if (block.level === 2) {
            <h2><ng-container *ngTemplateOutlet="inline; context: { $implicit: block.content }" /></h2>
          } @else {
            <h3><ng-container *ngTemplateOutlet="inline; context: { $implicit: block.content }" /></h3>
          }
        }
        @case ('paragraph') {
          <p><ng-container *ngTemplateOutlet="inline; context: { $implicit: block.content }" /></p>
        }
        @case ('list') {
          <ul>
            @for (item of block.items; track $index) {
              <li><ng-container *ngTemplateOutlet="inline; context: { $implicit: item }" /></li>
            }
          </ul>
        }
        @case ('table') {
          <div class="table-scroll">
            <table>
              <thead>
                <tr>
                  @for (cell of block.header; track $index) {
                    <th><ng-container *ngTemplateOutlet="inline; context: { $implicit: cell }" /></th>
                  }
                </tr>
              </thead>
              <tbody>
                @for (row of block.rows; track $index) {
                  <tr>
                    @for (cell of row; track $index) {
                      <td><ng-container *ngTemplateOutlet="inline; context: { $implicit: cell }" /></td>
                    }
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      }
    }
  `
})
export class LegalDocumentComponent {
  readonly content = input<string | null | undefined>('');
  readonly blocks = computed(() => parseLegalDocument(this.content()));
}
