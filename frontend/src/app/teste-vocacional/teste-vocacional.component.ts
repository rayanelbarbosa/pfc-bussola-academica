import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  Pergunta,
  RespostaDTO,
  ResultadoTesteResponse
} from '../models/teste-vocacional.models';
import { TesteVocacionalService } from '../teste-vocacional.service';

/**
 * Componente do questionario vocacional: exibe as perguntas fixas do banco
 * RIASEC, coleta as notas (1 a 5) e envia para o back-end calcular e
 * persistir o resultado (regra de negocio "pontuacao do teste vocacional").
 */
@Component({
  selector: 'app-teste-vocacional',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './teste-vocacional.component.html',
  styleUrl: './teste-vocacional.component.css'
})
export class TesteVocacionalComponent implements OnInit {
  perguntas: Pergunta[] = [];
  notas: Record<number, number> = {};
  resultado: ResultadoTesteResponse | null = null;
  carregandoPerguntas = false;
  enviando = false;
  mensagemErro = '';

  readonly escalaLikert = [1, 2, 3, 4, 5];

  constructor(private readonly service: TesteVocacionalService) {}

  ngOnInit(): void {
    this.carregandoPerguntas = true;
    this.service.listarPerguntas().subscribe({
      next: (perguntas) => {
        this.perguntas = perguntas;
        this.carregandoPerguntas = false;
      },
      error: () => {
        this.mensagemErro = 'Não foi possível carregar as perguntas do teste. Verifique se o back-end está em execução.';
        this.carregandoPerguntas = false;
      }
    });
  }

  get todasRespondidas(): boolean {
    return this.perguntas.length > 0 && this.perguntas.every((p) => !!this.notas[p.id]);
  }

  enviarRespostas(): void {
    if (!this.todasRespondidas) {
      this.mensagemErro = 'Responda todas as perguntas antes de enviar.';
      return;
    }

    const respostas: RespostaDTO[] = this.perguntas.map((p) => ({
      perguntaId: p.id,
      nota: this.notas[p.id]
    }));

    this.mensagemErro = '';
    this.enviando = true;
    this.service.submeterTeste({ respostas }).subscribe({
      next: (resultado) => {
        this.resultado = resultado;
        this.enviando = false;
      },
      error: () => {
        this.mensagemErro = 'Não foi possível calcular o resultado. Tente novamente.';
        this.enviando = false;
      }
    });
  }

  refazerTeste(): void {
    this.resultado = null;
    this.notas = {};
  }
}
