import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    title: 'Bússola Acadêmica',
    loadComponent: () => import('./features/home/home.component').then((m) => m.HomeComponent)
  },
  {
    path: 'teste-vocacional',
    title: 'Teste vocacional | Bússola Acadêmica',
    loadComponent: () =>
      import('./features/vocational-test/questionnaire/questionnaire.component').then((m) => m.QuestionnaireComponent)
  },
  {
    path: 'teste-vocacional/resultado/:id',
    title: 'Seu resultado | Bússola Acadêmica',
    loadComponent: () =>
      import('./features/vocational-test/result/result.component').then((m) => m.ResultComponent)
  },
  { path: '**', redirectTo: '' }
];
