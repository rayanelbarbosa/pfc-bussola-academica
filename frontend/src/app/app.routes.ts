import { Routes } from '@angular/router';

import { adminGuard, authGuard, guestGuard } from './core/guards/auth.guards';

export const routes: Routes = [
  {
    path: '',
    title: 'Bússola Acadêmica',
    canActivate: [authGuard],
    loadComponent: () => import('./features/home/home.component').then((m) => m.HomeComponent)
  },
  {
    path: 'login',
    title: 'Entrar | Bússola Acadêmica',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'cadastro',
    title: 'Criar conta | Bússola Acadêmica',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register/register.component').then((m) => m.RegisterComponent)
  },
  {
    path: 'teste-vocacional',
    title: 'Teste vocacional | Bússola Acadêmica',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/vocational-test/questionnaire/questionnaire.component').then((m) => m.QuestionnaireComponent)
  },
  {
    path: 'teste-vocacional/resultado/:id',
    title: 'Seu resultado | Bússola Acadêmica',
    canActivate: [authGuard],
    loadComponent: () => import('./features/vocational-test/result/result.component').then((m) => m.ResultComponent)
  },
  {
    path: 'meus-dados',
    title: 'Meus dados | Bússola Acadêmica',
    canActivate: [authGuard],
    loadComponent: () => import('./features/account/my-data/my-data.component').then((m) => m.MyDataComponent)
  },
  {
    path: 'admin/auditoria',
    title: 'Auditoria | Bússola Acadêmica',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./features/admin/audit-logs/audit-logs.component').then((m) => m.AuditLogsComponent)
  },
  {
    path: 'termos-de-uso',
    title: 'Termo de Uso | Bússola Acadêmica',
    loadComponent: () =>
      import('./features/legal/terms-of-use/terms-of-use.component').then((m) => m.TermsOfUseComponent)
  },
  {
    path: 'politica-de-privacidade',
    title: 'Política de Privacidade | Bússola Acadêmica',
    loadComponent: () =>
      import('./features/legal/privacy-policy/privacy-policy.component').then((m) => m.PrivacyPolicyComponent)
  },
  { path: '**', redirectTo: '' }
];
