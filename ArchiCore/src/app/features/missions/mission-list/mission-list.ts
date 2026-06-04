import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MissionControllerService } from '../../../api-client/api/missionController.service';
import { MissionResponse } from '../../../api-client/model/missionResponse';
import { AuthService } from '../../../core/services/auth.service';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { TooltipModule } from 'primeng/tooltip';
import { BreadcrumbComponent } from '../../../shared/breadcrumb/breadcrumb';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-mission-list',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    ButtonModule,
    CardModule,
    TagModule,
    TooltipModule,
    BreadcrumbComponent,
    RouterLink
  ],
  templateUrl: './mission-list.html'
})
export class MissionListComponent implements OnInit {
  private missionService = inject(MissionControllerService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private messageService = inject(MessageService);
  authService = inject(AuthService);

  codeDossier = signal<string>('');
  missions = signal<MissionResponse[]>([]);
  loading = signal<boolean>(true);

  // Codes missions sur lesquels l'ADMIN courant a des droits
  assignedMissions = signal<string[]>([]);

  // L'utilisateur peut créer une mission ?
  canCreate = computed(() => {
    return this.authService.hasRole(['SUPER_ADMIN', 'ADMIN_PRINCIPAL']);
  });

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.codeDossier.set(params['codeDossier']);
      this.loadMissions();
      this.loadAssignedMissions();
    });
  }

  loadMissions() {
    this.loading.set(true);
    this.missionService.findAll().subscribe({
      next: (allMissions) => {
        const filtered = (allMissions || []).filter(m => m.codeDossier === this.codeDossier());
        this.missions.set(filtered);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erreur missions', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger la liste des missions'
        });
        this.loading.set(false);
      }
    });
  }

  // Charge les missions assignées à l'ADMIN courant
  loadAssignedMissions() {
    if (this.authService.hasRole(['SUPER_ADMIN', 'ADMIN_PRINCIPAL'])) {
      // Accès total — pas besoin de charger
      this.assignedMissions.set([]);
      return;
    }

    if (this.authService.hasRole(['ADMIN'])) {
      this.missionService.getMyAssignedMissions().subscribe({
        next: (codes) => this.assignedMissions.set(codes),
        error: (err) => {
          console.error('Erreur missions assignées', err);
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible de vérifier vos assignations de missions'
          });
          this.assignedMissions.set([]);
        }
      });
    }
  }

  // Peut-on modifier cette mission spécifique ?
  canEditMission(codeMission: string): boolean {
    if (this.authService.hasRole(['SUPER_ADMIN', 'ADMIN_PRINCIPAL'])) return true;
    if (this.authService.hasRole(['ADMIN'])) {
      return this.assignedMissions().includes(codeMission);
    }
    return false;
  }

  getStatusSeverity(status: string | undefined): "success" | "info" | "warn" | "danger" | "secondary" | "contrast" {
    switch (status) {
      case 'TERMINEE': return 'success';
      case 'EN_COURS': return 'info';
      case 'PLANIFIEE': return 'warn';
      case 'SUSPENDUE': return 'danger';
      default: return 'secondary';
    }
  }

  viewDetail(codeMission: string) {
    this.router.navigate(['/dossiers', this.codeDossier(), 'missions', codeMission, 'detail']);
  }

  viewDocuments(codeMission: string) {
    this.router.navigate(['/dossiers', this.codeDossier(), 'missions', codeMission, 'documents']);
  }

  editMission(codeMission: string) {
    this.router.navigate(['/dossiers', this.codeDossier(), 'missions', codeMission, 'edit']);
  }
}