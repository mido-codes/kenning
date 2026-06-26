import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import type { Observable } from 'rxjs';
import { catchError, map, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  readonly isAuthenticated = signal<boolean | null>(null);

  checkAuth(): Observable<boolean> {
    return this.http.get<unknown[]>('/api/documents').pipe(
      map(() => {
        this.isAuthenticated.set(true);
        return true;
      }),
      catchError(() => {
        this.isAuthenticated.set(false);
        return of(false);
      }),
    );
  }

  login(): void {
    window.location.href = '/oauth2/authorization/google';
  }

  logout(): Observable<void> {
    return this.http.post<void>('/api/logout', null);
  }
}
