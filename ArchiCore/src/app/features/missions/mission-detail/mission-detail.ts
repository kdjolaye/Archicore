import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

// Services API
import { MissionControllerService } from '../../../api-client/api/missionController.service';
import { UserControllerService } from '../../../api-client/api/userController.service';
import { AuthService } from '../../../core/services/auth.service';

// Models
import { MissionResponse } from '../../../api-client/model/missionResponse';
import { EquipeResponse } from '../../../api-client/model/equipeResponse';
import { EquipeCreateRequest } from '../../../api-client/model/equipeCreateRequest';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { BreadcrumbComponent } from '../../../shared/breadcrumb/breadcrumb';
import { MessageService, ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-mission-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    CardModule,
    TagModule,
    TableModule,
    DialogModule,
    SelectModule,
    BreadcrumbComponent,
    RouterLink
  ],
  templateUrl: './mission-detail.html'
})
export class MissionDetailComponent implements OnInit {
  private missionService = inject(MissionControllerService);
  private userService = inject(UserControllerService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  authService = inject(AuthService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);

  mission = signal<MissionResponse | null>(null);
  loading = signal(true);
  codeDossier = signal<string | null>(null);
  isAssigned = signal<boolean>(false); // ← ADMIN assigné à cette mission ?

  // États de l'équipe
  team = signal<EquipeResponse[]>([]);
  allUsers = signal<any[]>([]);
  loadingTeam = signal(false);
  showAddTeamMemberDialog = signal(false);
  isSubmittingTeamMember = signal(false);

  // Formulaire d'ajout
  selectedUserId = signal<number | null>(null);
  selectedPoste = signal<EquipeCreateRequest.PosteEnum>('JUNIOR');

  // Options de postes
  posteOptions = [
    { label: 'Chef de Mission', value: 'CHEF_MISSION' },
    { label: 'Superviseur', value: 'SUPERVISEUR' },
    { label: 'Senior', value: 'SENIOR' },
    { label: 'Junior', value: 'JUNIOR' },
    { label: 'Assistant', value: 'ASSISTANT' },
    { label: 'Stagiaire', value: 'STAGIAIRE' }
  ];

  // Options d'utilisateurs disponibles (filtrée pour la modale)
  availableUsers = computed(() => {
    const currentTeamIds = this.team().map(member => member.userId);
    return this.allUsers()
      .filter(u => !currentTeamIds.includes(u.id))
      .map(u => ({
        label: `${u.surname || ''} ${u.name || ''} (${u.username || ''})`,
        value: u.id
      }));
  });

  // canEdit calculé automatiquement selon le rôle + assignation
  canEdit = computed(() => {
    const user = this.authService.currentUser();
    if (!user) return false;

    // SUPER_ADMIN et ADMIN_PRINCIPAL : accès total
    if (this.authService.hasRole(['SUPER_ADMIN', 'ADMIN_PRINCIPAL'])) return true;

    // ADMIN : uniquement si assigné à cette mission
    if (this.authService.hasRole(['ADMIN'])) return this.isAssigned();

    return false;
  });

  ngOnInit() {
    this.codeDossier.set(this.route.snapshot.paramMap.get('codeDossier'));
    const code = this.route.snapshot.paramMap.get('codeMission');
    if (code) {
      this.loadMission(code);
      this.checkAssignment(code); // ← vérifie l'assignation
      this.loadTeam(code);
      this.loadUsers();
    }
  }

  loadMission(code: string) {
    this.loading.set(true);
    this.missionService.findByCode(code).subscribe({
      next: (data) => {
        this.mission.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erreur chargement mission', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger les détails de la mission'
        });
        this.loading.set(false);
      }
    });
  }

  checkAssignment(codeMission: string) {
    const user = this.authService.currentUser();
    if (!user) return;

    // SUPER_ADMIN et ADMIN_PRINCIPAL : toujours autorisés, pas besoin de vérifier
    if (this.authService.hasRole(['SUPER_ADMIN', 'ADMIN_PRINCIPAL'])) {
      this.isAssigned.set(true);
      return;
    }

    // ADMIN : appel au nouvel endpoint dédié
    if (this.authService.hasRole(['ADMIN'])) {
      this.missionService.isAssignedToMe(codeMission).subscribe({
        next: (assigned) => this.isAssigned.set(assigned),
        error: (err) => {
          console.error('Erreur vérification assignation', err);
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Impossible de vérifier vos droits sur cette mission'
          });
          this.isAssigned.set(false);
        }
      });
    }
  }

  deleteMission() {
    const code = this.mission()?.codeMission;
    if (!code) return;

    this.confirmationService.confirm({
      message: `Voulez-vous vraiment supprimer la mission ${code} ? Tous les documents associés seront perdus.`,
      header: 'Confirmation de suppression',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Supprimer',
      rejectLabel: 'Annuler',
      acceptButtonStyleClass: 'p-button-danger rounded-xl',
      rejectButtonStyleClass: 'p-button-text text-slate-500 rounded-xl',
      accept: () => {
        this.missionService._delete(code).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Supprimé', detail: 'Mission supprimée' });
            setTimeout(() => this.router.navigate(['/dossiers', this.codeDossier(), 'missions']), 1000);
          },
          error: (err) => {
            this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de la suppression' });
            console.error('Erreur suppression mission', err);
          }
        });
      }
    });
  }

canDelete = computed(() => {
    return this.authService.hasRole(['SUPER_ADMIN', 'ADMIN_PRINCIPAL']);
  });

  loadTeam(code: string) {
    this.loadingTeam.set(true);
    this.missionService.getEquipeByMission(code).subscribe({
      next: (data) => {
        this.team.set(data || []);
        this.loadingTeam.set(false);
      },
      error: (err) => {
        console.error('Erreur chargement équipe', err);
        this.loadingTeam.set(false);
      }
    });
  }

  loadUsers() {
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.allUsers.set(data || []);
      },
      error: (err) => {
        console.error('Erreur chargement utilisateurs', err);
      }
    });
  }

  openAddTeamMemberDialog() {
    this.selectedUserId.set(null);
    this.selectedPoste.set('JUNIOR');
    this.showAddTeamMemberDialog.set(true);
  }

  onAddTeamMemberSubmit() {
    const code = this.mission()?.codeMission;
    const userId = this.selectedUserId();
    const poste = this.selectedPoste();
    
    if (!code || !userId || !poste) return;

    this.isSubmittingTeamMember.set(true);
    this.missionService.addMembreEquipe(code, { codeMission: code, userId, poste }).subscribe({
      next: () => {
        this.isSubmittingTeamMember.set(false);
        this.showAddTeamMemberDialog.set(false);
        this.loadTeam(code);
        this.messageService.add({
          severity: 'success',
          summary: 'Membre ajouté',
          detail: 'Le collaborateur a bien été ajouté à l\'équipe de cette mission.'
        });
      },
      error: (err) => {
        this.isSubmittingTeamMember.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: err.error?.message || 'Erreur lors de l\'ajout du membre.'
        });
      }
    });
  }

  getUserInfo(userId: number | undefined) {
    if (!userId) return { displayName: 'Inconnu', username: '', role: '' };
    const user = this.allUsers().find(u => u.id === userId);
    if (!user) return { displayName: `Utilisateur #${userId}`, username: '', role: '' };
    return {
      displayName: `${user.surname || ''} ${user.name || ''}`,
      username: user.username,
      role: user.role
    };
  }

  getRoleLabel(role: string | undefined): string {
    const labels: Record<string, string> = {
      SUPER_ADMIN: 'Super Administrateur',
      ADMIN_PRINCIPAL: 'Administrateur Principal',
      ADMIN: 'Administrateur',
      CONSULTANT: 'Consultant'
    };
    return role ? (labels[role] || role) : '—';
  }

  getPosteLabel(poste: string | undefined): string {
    const labels: Record<string, string> = {
      CHEF_MISSION: 'Chef de Mission',
      SUPERVISEUR: 'Superviseur',
      SENIOR: 'Senior',
      JUNIOR: 'Junior',
      ASSISTANT: 'Assistant',
      STAGIAIRE: 'Stagiaire'
    };
    return poste ? (labels[poste] || poste) : '—';
  }

  getStatutSeverity(statut: string | undefined): "success" | "info" | "warn" | "danger" | "secondary" | "contrast" {
    switch (statut) {
      case 'EN_COURS': return 'info';
      case 'TERMINEE': return 'success';
      case 'PLANIFIEE': return 'warn';
      case 'SUSPENDUE': return 'danger';
      default: return 'secondary';
    }
  }
}