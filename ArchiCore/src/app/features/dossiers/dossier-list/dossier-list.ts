import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { DossierControllerService } from '../../../api-client/api/dossierController.service';
import { DossierResponse } from '../../../api-client/model/dossierResponse';
import { AuthService } from '../../../core/services/auth.service';
import { TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { CardModule } from 'primeng/card';
import { BreadcrumbComponent } from '../../../shared/breadcrumb/breadcrumb';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-dossier-list',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    InputTextModule,
    ButtonModule,
    IconFieldModule,
    InputIconModule,
    CardModule,
    BreadcrumbComponent,
    RouterLink
  ],
  templateUrl: './dossier-list.html'
})
export class DossierListComponent implements OnInit {
  private dossierService = inject(DossierControllerService);
  private router = inject(Router);
  private messageService = inject(MessageService);
  authService = inject(AuthService);

  // État de chargement et liste des dossiers (Signals)
  dossiers = signal<DossierResponse[]>([]);
  loading = signal<boolean>(true);

  ngOnInit() {
    this.loadDossiers();
  }

  /* 
    Charge la liste de tous les dossiers depuis l'API 
  */
  loadDossiers() {
    this.loading.set(true);
    this.dossierService.getAll().subscribe({
      next: (data) => {
        this.dossiers.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des dossiers', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger les dossiers'
        });
        this.loading.set(false);
      }
    });
  }

  viewDetail(code: string | undefined) {
    if (code) {
      this.router.navigate(['/dossiers', code, 'detail']);
    }
  }

  viewMissions(code: string | undefined) {
    if (code) {
      this.router.navigate(['/dossiers', code, 'missions']);
    }
  }

  editDossier(code: string | undefined) {
    if (code) {
      this.router.navigate(['/dossiers', code, 'edit']);
    }
  }
}
