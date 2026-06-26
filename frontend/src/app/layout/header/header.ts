import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { TooltipModule } from 'primeng/tooltip';
import { Toast } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../core/services/auth.service';
import { ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.html',
  styleUrl: './header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, TooltipModule, Toast],
})
export class Header {
  readonly auth = inject(AuthService);
  readonly theme = inject(ThemeService);

  private readonly router = inject(Router);
  private readonly messageService = inject(MessageService);

  readonly loggingOut = signal(false);

  logout(): void {
    if (this.loggingOut()) return;
    this.loggingOut.set(true);

    this.auth.logout().subscribe({
      next: () => this.completeLogout(),
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Sign out failed',
          detail: 'Could not reach the server. You have been signed out locally.',
        });
        this.completeLogout();
      },
    });
  }

  private completeLogout(): void {
    this.auth.isAuthenticated.set(false);
    this.loggingOut.set(false);
    this.router.navigate(['/login']);
  }
}
