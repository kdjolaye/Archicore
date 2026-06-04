import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { MenuItem } from 'primeng/api';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';

@Component({
  selector: 'app-breadcrumb',
  standalone: true,
  imports: [CommonModule, BreadcrumbModule],
  templateUrl: './breadcrumb.html'
})
export class BreadcrumbComponent {
  private router = inject(Router);

  items = signal<MenuItem[]>([]);
  home: MenuItem = { icon: 'pi pi-home', routerLink: '/dossiers' };

  constructor() {
    // Génère au chargement initial
    this.updateBreadcrumbs();

    // Met à jour à chaque navigation
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.updateBreadcrumbs();
    });
  }

  private updateBreadcrumbs() {
    const url = this.router.url.split('?')[0];
    const parts = url.split('/').filter(p => p !== '');

    // Labels traduits pour les segments connus
    const labelMap: Record<string, string> = {
      'dossiers'  : 'Dossiers',
      'missions'  : 'Missions',
      'documents' : 'Documents',
      'new'       : 'Nouveau',
      'edit'      : 'Modifier',
      'detail'    : 'Détails',
    };

    // Structure attendue :
    // dossiers / {codeDossier} / missions / {codeMission} / documents|detail|edit
    // Index :  0       1            2           3                  4

    const breadcrumbs: MenuItem[] = parts.map((part, index) => {
      const label = labelMap[part.toLowerCase()] ?? part;

      let routerLink: string;

      // ─── Règles de navigation par position ──────────────────────────────
      if (index === 0) {
        // "Dossiers" → liste des dossiers
        routerLink = '/dossiers';

      } else if (index === 1) {
        // "{codeDossier}" → détail du dossier
        routerLink = `/dossiers/${parts[1]}/detail`;

      } else if (index === 2) {
        // "Missions" → liste des missions du dossier
        routerLink = `/dossiers/${parts[1]}/missions`;

      } else if (index === 3) {
        // "{codeMission}" → détail de la mission
        routerLink = `/dossiers/${parts[1]}/missions/${parts[3]}/detail`;

      } else {
        // "Documents", "Modifier", "Détails", "Nouveau" → lien complet
        routerLink = '/' + parts.slice(0, index + 1).join('/');
      }

      return { label, routerLink };
    });

    this.items.set(breadcrumbs);
  }
}