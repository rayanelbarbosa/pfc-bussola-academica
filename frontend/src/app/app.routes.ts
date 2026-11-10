import { Routes } from '@angular/router';

import { adminGuard, authGuard, guestGuard } from './core/guards/auth.guards';

export const routes: Routes = [
  {
    path: '',
    title: 'Bússola Acadêmica | Formas de ingresso e teste vocacional',
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
    path: 'esqueci-senha',
    title: 'Esqueci minha senha | Bússola Acadêmica',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/forgot-password/forgot-password.component').then((m) => m.ForgotPasswordComponent)
  },
  {
    path: 'redefinir-senha',
    title: 'Criar nova senha | Bússola Acadêmica',
    loadComponent: () =>
      import('./features/auth/reset-password/reset-password.component').then((m) => m.ResetPasswordComponent)
  },
  {
    path: 'novo-aceite',
    title: 'Termos atualizados | Bússola Acadêmica',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/auth/terms-update/terms-update.component').then((m) => m.TermsUpdateComponent)
  },
  {
    path: 'teste-vocacional',
    title: 'Teste vocacional | Bússola Acadêmica',
    canActivate: [authGuard],
    loadComponent: () => import('./features/vocational-test/areas/areas.component').then((m) => m.AreasComponent)
  },
  {
    path: 'teste-vocacional/perguntas',
    title: 'Teste vocacional | Bússola Acadêmica',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/vocational-test/questions/questions.component').then((m) => m.QuestionsComponent)
  },
  {
    path: 'teste-vocacional/resultado/:id',
    title: 'Seu resultado | Bússola Acadêmica',
    canActivate: [authGuard],
    loadComponent: () => import('./features/vocational-test/result/result.component').then((m) => m.ResultComponent)
  },
  {
    path: 'meus-resultados',
    title: 'Meus resultados | Bússola Acadêmica',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/account/my-results/my-results.component').then((m) => m.MyResultsComponent)
  },
  {
    path: 'meus-dados',
    title: 'Meus dados | Bússola Acadêmica',
    canActivate: [authGuard],
    loadComponent: () => import('./features/account/my-data/my-data.component').then((m) => m.MyDataComponent)
  },
  {
    path: 'admin',
    title: 'Dashboard | Administração',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/dashboard/dashboard.component').then((m) => m.DashboardComponent)
  },
  {
    path: 'admin/formas-de-ingresso',
    title: 'Formas de ingresso | Administração',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./features/admin/admission-methods/admission-methods.component').then((m) => m.AdmissionMethodsComponent)
  },
  {
    path: 'admin/carreiras',
    title: 'Carreiras | Administração',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/careers/careers.component').then((m) => m.CareersComponent)
  },
  {
    path: 'admin/usuarios',
    title: 'Usuários | Administração',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/users/users.component').then((m) => m.UsersComponent)
  },
  {
    path: 'admin/termos',
    title: 'Termos e política | Administração',
    canActivate: [adminGuard],
    loadComponent: () => import('./features/admin/terms/terms.component').then((m) => m.TermsComponent)
  },
  {
    path: 'admin/auditoria',
    title: 'Auditoria | Administração',
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
  {
    path: 'acesso-negado',
    title: 'Acesso negado | Bússola Acadêmica',
    data: { code: '403', heading: 'Acesso negado', description: 'Esta área é exclusiva para administradores.' },
    loadComponent: () => import('./features/errors/error-page.component').then((m) => m.ErrorPageComponent)
  },
  {
    path: '**',
    title: 'Página não encontrada | Bússola Acadêmica',
    loadComponent: () => import('./features/errors/error-page.component').then((m) => m.ErrorPageComponent)
  }
];
