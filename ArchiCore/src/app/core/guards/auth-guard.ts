// auth.guard.ts
import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { LoginModalService } from '../../shared/login-modal/login-modal';
import { catchError, map, of } from 'rxjs';

export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
  const authService = inject(AuthService);
  const modalService = inject(LoginModalService);
  const router = inject(Router);

  const checkUserStatus = () => {
    const user = authService.currentUser();
    if (!user) return false;
    
    // Si l'utilisateur doit changer son mot de passe
    if (user.status === 'FORCE_PASSWORD_CHANGE' || user.status === 'PASSWORD_EXPIRED') {
      if (state.url !== '/change-password') {
        return router.parseUrl('/change-password');
      }
      return true;
    }
    
    // S'il est sur la page /change-password mais qu'il est ACTIVE, on le redirige vers l'accueil
    if (user.status === 'ACTIVE' && state.url === '/change-password') {
      return router.parseUrl('/dossiers');
    }

    return true;
  };

  // Déjà connecté en mémoire → vérif statut et accès direct
  if (authService.currentUser()) {
    return checkUserStatus();
  }

  // Pas en mémoire → tente un refresh (cookie refreshToken peut exister)
  return authService.refreshToken().pipe(
    map(() => {
      if (authService.currentUser()) return checkUserStatus();
      modalService.show(false);
      return false;
    }),
    catchError(() => {
      // Pas de cookie valide → ouvre le modal login
      modalService.show(false);
      return of(false);
    })
  );
};