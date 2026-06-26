import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'documents', pathMatch: 'full' },
  {
    path: 'documents',
    loadComponent: () =>
      import('./documents/document-list/document-list').then(
        (m) => m.DocumentList,
      ),
    canActivate: [authGuard],
  },
  {
    path: 'conversations/:id',
    loadComponent: () =>
      import('./conversations/chat/chat').then((m) => m.Chat),
    canActivate: [authGuard],
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./auth/login/login').then((m) => m.Login),
  },
  { path: '**', redirectTo: 'documents' },
];
