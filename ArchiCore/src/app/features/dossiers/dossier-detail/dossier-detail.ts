import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DossierControllerService } from '../../../api-client/api/dossierController.service';
import { DossierResponse } from '../../../api-client/model/dossierResponse';
import { AuthService } from '../../../core/services/auth.service';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TooltipModule } from 'primeng/tooltip';
import { BreadcrumbComponent } from '../../../shared/breadcrumb/breadcrumb';
import { MessageService, ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-dossier-detail',
  standalone: true,
  imports: [
    CommonModule,
    ButtonModule,
    CardModule,
    TooltipModule,
    BreadcrumbComponent,
    RouterLink
  ],
  templateUrl: './dossier-detail.html'
})
export class DossierDetailComponent implements OnInit {
  private dossierService = inject(DossierControllerService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  authService = inject(AuthService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);

  dossier = signal<DossierResponse | null>(null);
  loading = signal(true);

  // SUPER_ADMIN et ADMIN_PRINCIPAL uniquement
  canEdit = computed(() =>
    this.authService.hasRole(['SUPER_ADMIN', 'ADMIN_PRINCIPAL'])
  );

  ngOnInit() {
    const code = this.route.snapshot.paramMap.get('codeDossier');
    if (code) this.loadDossier(code);
  }

  loadDossier(code: string) {
    this.loading.set(true);
    this.dossierService.getByCode(code).subscribe({
      next: (data) => {
        this.dossier.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erreur chargement dossier', err);
        this.loading.set(false);
      }
    });
  }

  deleteDossier() {
    const code = this.dossier()?.codeDossier;
    if (!code) return;

    this.confirmationService.confirm({
      message: `Voulez-vous vraiment supprimer le dossier ${code} ? Cette action est irréversible.`,
      header: 'Confirmation de suppression',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Supprimer',
      rejectLabel: 'Annuler',
      acceptButtonStyleClass: 'p-button-danger rounded-xl',
      rejectButtonStyleClass: 'p-button-text text-slate-500 rounded-xl',
      accept: () => {
        this.dossierService.delete1(code).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Supprimé', detail: 'Dossier supprimé' });
            setTimeout(() => this.router.navigate(['/dossiers']), 1000);
          },
          error: (err) => {
            this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de la suppression' });
            console.error('Erreur suppression', err);
          }
        });
      }
    });
  }
}