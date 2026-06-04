import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ThemeService } from '../../core/services/theme.service';
import { AuthService } from '../../core/services/auth.service';
import { LoginModalService } from '../login-modal/login-modal';
import { ButtonModule } from 'primeng/button';
import { ToolbarModule } from 'primeng/toolbar';
import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { MenuItem } from 'primeng/api';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    ToolbarModule,
    AvatarModule,
    MenuModule,
    TooltipModule
  ],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent {
  // Injection des services
  themeService = inject(ThemeService);
  authService = inject(AuthService);
  modalService = inject(LoginModalService);
  router = inject(Router);

  // Éléments du menu utilisateur (PrimeNG MenuItem)
  userMenuItems: MenuItem[] = [
    {
      label: 'Profil',
      icon: 'pi pi-user',
      command: () => this.router.navigate(['/profile'])
    },
    {
      separator: true
    },
    {
      label: 'Déconnexion',
      icon: 'pi pi-power-off',
      command: () => this.authService.logout()
    }
  ];

  /* 
    Méthode pour basculer le thème. 
    On appelle simplement le service qui gère la classe CSS et le signal.
  */
  toggleTheme() {
    this.themeService.toggleTheme();
  }

  /* 
    Ouvre la modal de connexion
  */
  showLogin() {
    this.modalService.show();
  }
}
