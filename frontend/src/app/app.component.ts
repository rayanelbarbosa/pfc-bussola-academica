import { Component } from '@angular/core';
import { TesteVocacionalComponent } from './teste-vocacional/teste-vocacional.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [TesteVocacionalComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'bussola-academica';
}
