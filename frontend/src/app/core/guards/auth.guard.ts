import { inject } from '@angular/core';
import type { CanActivateFn } from '@angular/router';
import { Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isAuthenticated() !== null) {
    return auth.isAuthenticated() ? true : router.createUrlTree(['/login']);
  }

  return auth.checkAuth().pipe(
    map((ok) => (ok ? true : router.createUrlTree(['/login']))),
  );
};
