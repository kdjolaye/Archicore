import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap, catchError, map } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthControllerService } from '../../api-client/api/authController.service';
import { UserControllerService } from '../../api-client/api/userController.service';
import { AuthResponse } from '../../api-client/model/authResponse';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authControllerService = inject(AuthControllerService);
  private userControllerService = inject(UserControllerService);
  private router = inject(Router);

  // ─── État de l'utilisateur courant ───────────────────────────────────────
  currentUser = signal<AuthResponse | null>(null);

  constructor() {
    this.fetchCurrentUser();
  }

  fetchCurrentUser() {
    this.authControllerService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser.set(user);
      },
      error: () => {
        this.clearSession();
      }
    });
  }

  // ─── Connexion ────────────────────────────────────────────────────────────
  login(credentials: { email: string; password: string }) {
  return this.authControllerService.login({
    username: credentials.email,
    password: credentials.password
  }).pipe(
    tap(response => {
      
      this.currentUser.set(response);
      
    })
  );
}

  // ─── Refresh du token ─────────────────────────────────────────────────────
  // Appelé automatiquement par le jwtInterceptor quand un 401 est reçu
 refreshToken() {
  return this.authControllerService.refreshToken().pipe(
    tap(response => {
      this.currentUser.set(response);
    })
    // ← pas de catchError ici, l'intercepteur gère le cas d'échec
  );
}

  // ─── Déconnexion ──────────────────────────────────────────────────────────
  logout() {
    this.authControllerService.logout().pipe(
      catchError(() => of(null))
    ).subscribe(() => {
      this.clearSession();
      this.router.navigate(['/']);
    });
  }

  // ─── Vide la session sans appel backend ───────────────────────────────────
  // Utilisé par le jwtInterceptor quand le refresh échoue
  clearSession() {
    this.currentUser.set(null);
  }

  // ─── Profil & Sécurité ───────────────────────────────────────────────────
  
  changePassword(data: { currentPassword: string; newPassword: string }) {
    return this.userControllerService.changePassword(data).pipe(
      tap(() => {
        // Mettre à jour le statut localement après succès
        const user = this.currentUser();
        if (user) {
          this.currentUser.set({ ...user, status: 'ACTIVE' } as AuthResponse);
        }
      })
    );
  }

  updateProfile(data: { name: string; surname: string; phone?: string }) {
    return this.userControllerService.updateProfile(data).pipe(
      tap(() => {
        // En cas de succès, on pourrait mettre à jour le nom/prénom si stocké, mais l'AuthResponse actuelle n'a que email/role/status.
        // Si besoin de forcer un refresh complet on peut le faire.
      })
    );
  }

  // ─── Vérification des rôles ───────────────────────────────────────────────
  // Utilisé dans les composants et guards pour afficher/masquer des éléments
  hasRole(roles: string[]): boolean {
    const user = this.currentUser();
    if (!user || !user.role) return false;
    return roles.some(r => user.role === r || user.role === `ROLE_${r}`);
  }

  // ─── Vérifie si l'utilisateur est connecté ────────────────────────────────
  isAuthenticated(): boolean {
    return this.currentUser() !== null;
  }
}