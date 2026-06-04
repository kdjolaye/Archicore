import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UserControllerService } from '../../../api-client/api/userController.service';
import { MissionControllerService } from '../../../api-client/api/missionController.service';
import { RoleControllerService } from '../../../api-client/api/roleController.service';
import { UserResponse } from '../../../api-client/model/userResponse';
import { MissionResponse } from '../../../api-client/model/missionResponse';
import { AuthService } from '../../../core/services/auth.service';
import { RouterLink } from '@angular/router';

// PrimeNG
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { InputTextModule } from 'primeng/inputtext';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    CardModule,
    DialogModule,
    SelectModule,
    InputTextModule,
    TooltipModule,
    RouterLink
  ],
  templateUrl: './user-list.html'
})
export class UserListComponent implements OnInit {
  private userService = inject(UserControllerService);
  private roleService = inject(RoleControllerService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);
  authService = inject(AuthService);

  users = signal<UserResponse[]>([]);
  loading = signal(true);
  
  // Create user dialog
  showCreateDialog = signal(false);
  createForm!: FormGroup;
  isSubmitting = signal(false);
  tempPassword = signal<string | null>(null);

  // Assign mission dialog
  private missionService = inject(MissionControllerService);
  showAssignMissionDialog = signal(false);
  selectedAdminUser = signal<UserResponse | null>(null);
  availableMissions = signal<MissionResponse[]>([]);
  assignedMissions = signal<MissionResponse[]>([]);
  loadingMissions = signal(false);
  isAssigning = signal(false);
  selectedMissionCode = signal<string | null>(null);

  private rolesMap: Record<string, string> = {
    SUPER_ADMIN: 'Super Administrateur',
    ADMIN_PRINCIPAL: 'Administrateur Principal',
    ADMIN: 'Administrateur',
    CONSULTANT: 'Consultant'
  };

  roles: { label: string; value: string }[] = [
    { label: 'Super Administrateur', value: 'SUPER_ADMIN' },
    { label: 'Administrateur Principal', value: 'ADMIN_PRINCIPAL' },
    { label: 'Administrateur', value: 'ADMIN' },
    { label: 'Consultant', value: 'CONSULTANT' }
  ];

  ngOnInit() {
    this.initForm();
    this.loadRoles();
    this.loadUsers();
  }

  loadRoles() {
    this.roleService.getAllRoles().subscribe({
      next: (data) => {
        if (data && data.length > 0) {
          this.roles = data.map(r => ({
            label: this.rolesMap[r.name!] || r.name || '',
            value: r.name || ''
          }));
        }
      },
      error: (err) => {
        console.error('Erreur chargement rôles, utilisation de la liste par défaut', err);
      }
    });
  }

  initForm() {
    this.createForm = this.fb.group({
      username: ['', [Validators.required, Validators.email]],
      roleName: ['', [Validators.required]]
    });
  }

  loadUsers() {
    this.loading.set(true);
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.users.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de charger les utilisateurs' });
      }
    });
  }

  openCreateDialog() {
    this.createForm.reset();
    this.tempPassword.set(null);
    this.showCreateDialog.set(true);
  }

  onCreateSubmit() {
    if (this.createForm.invalid) return;

    this.isSubmitting.set(true);
    const { username, roleName } = this.createForm.value;

    this.userService.createUser({ username, roleName }).subscribe({
      next: (res: any) => {
        this.isSubmitting.set(false);
        const data = typeof res === 'string' ? JSON.parse(res) : res;
        this.tempPassword.set(data.temporaryPassword || data['temporaryPassword']);
        this.loadUsers();
        this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Utilisateur créé' });
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.messageService.add({ severity: 'error', summary: 'Erreur', detail: err.error?.message || 'Erreur lors de la création' });
      }
    });
  }

  copyPassword() {
    const pwd = this.tempPassword();
    if (pwd) {
      navigator.clipboard.writeText(pwd).then(() => {
        this.messageService.add({ severity: 'info', summary: 'Copié', detail: 'Mot de passe copié dans le presse-papier' });
      });
    }
  }

  closeDialog() {
    this.showCreateDialog.set(false);
    this.tempPassword.set(null);
  }

  toggleStatus(user: UserResponse) {
    const isSuspended = user.status === 'SUSPENDED';
    const action = isSuspended ? 'activer' : 'suspendre';
    
    this.confirmationService.confirm({
      message: `Voulez-vous vraiment ${action} le compte de ${user.username} ?`,
      header: 'Confirmation',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Oui',
      rejectLabel: 'Non',
      acceptButtonStyleClass: isSuspended ? 'p-button-success' : 'p-button-danger',
      accept: () => {
        const obs$ = isSuspended 
          ? this.userService.activateUser(user.username!) 
          : this.userService.suspendUser(user.username!);

        obs$.subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Succès', detail: `Utilisateur ${isSuspended ? 'activé' : 'suspendu'}` });
            this.loadUsers();
          },
          error: () => {
            this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Opération impossible' });
          }
        });
      }
    });
  }

  getRoleLabel(role: string | undefined): string {
    const found = this.roles.find(r => r.value === role);
    return found ? found.label : (role || '—');
  }

  getStatusBadgeClass(status: string | undefined): string {
    switch (status) {
      case 'ACTIVE': return 'bg-emerald-100 text-emerald-700 dark:bg-emerald-500/20 dark:text-emerald-400 border border-emerald-200 dark:border-emerald-500/30';
      case 'SUSPENDED': return 'bg-slate-100 text-slate-700 dark:bg-slate-700 dark:text-slate-300 border border-slate-200 dark:border-slate-600';
      case 'FORCE_PASSWORD_CHANGE': return 'bg-amber-100 text-amber-700 dark:bg-amber-500/20 dark:text-amber-400 border border-amber-200 dark:border-amber-500/30';
      case 'PASSWORD_EXPIRED': return 'bg-red-100 text-red-700 dark:bg-red-500/20 dark:text-red-400 border border-red-200 dark:border-red-500/30';
      default: return 'bg-gray-100 text-gray-700';
    }
  }

  getStatusLabel(status: string | undefined): string {
    switch (status) {
      case 'ACTIVE': return 'Actif';
      case 'SUSPENDED': return 'Suspendu';
      case 'FORCE_PASSWORD_CHANGE': return 'Mot de passe temporaire';
      case 'PASSWORD_EXPIRED': return 'Mot de passe expiré';
      default: return status || '—';
    }
  }

  // --- Assignation Mission ---
  openAssignMissionDialog(user: UserResponse) {
    this.selectedAdminUser.set(user);
    this.selectedMissionCode.set(null);
    this.showAssignMissionDialog.set(true);
    if (user.id) {
      this.loadUserMissions(user.id);
    }
    this.loadAvailableMissions();
  }

  loadUserMissions(userId: number) {
    this.loadingMissions.set(true);
    this.missionService.getMissionsAssignedToUser(userId).subscribe({
      next: (missions) => {
        this.assignedMissions.set(missions);
        this.loadingMissions.set(false);
      },
      error: () => {
        this.loadingMissions.set(false);
        this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de charger les missions assignées' });
      }
    });
  }

  loadAvailableMissions() {
    this.loadingMissions.set(true);
    this.missionService.getAvailableForAdmin().subscribe({
      next: (missions) => {
        this.availableMissions.set(missions);
        this.loadingMissions.set(false);
      },
      error: () => {
        this.loadingMissions.set(false);
        this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de charger les missions disponibles' });
      }
    });
  }

  closeAssignDialog() {
    this.showAssignMissionDialog.set(false);
    this.selectedAdminUser.set(null);
    this.selectedMissionCode.set(null);
    this.assignedMissions.set([]);
    this.availableMissions.set([]);
  }

  onAssignMission() {
    const code = this.selectedMissionCode();
    const user = this.selectedAdminUser();
    if (!code || !user || !user.id) return;

    this.isAssigning.set(true);
    this.missionService.assignUser(code, { userId: user.id }).subscribe({
      next: () => {
        this.isAssigning.set(false);
        this.messageService.add({ severity: 'success', summary: 'Succès', detail: `Mission ${code} assignée à ${user.name}` });
        this.selectedMissionCode.set(null);
        this.loadUserMissions(user.id!);
        this.loadAvailableMissions();
      },
      error: (err) => {
        this.isAssigning.set(false);
        this.messageService.add({ severity: 'error', summary: 'Erreur', detail: err.error?.message || 'Erreur lors de l\'assignation' });
      }
    });
  }

  onUnassignMission(codeMission: string) {
    const user = this.selectedAdminUser();
    if (!user || !user.id) return;

    this.confirmationService.confirm({
      message: `Êtes-vous sûr de vouloir retirer l'assignation de la mission ${codeMission} pour ${user.name} ?`,
      header: 'Confirmation de désassignation',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Retirer',
      rejectLabel: 'Annuler',
      acceptButtonStyleClass: 'p-button-danger rounded-xl',
      rejectButtonStyleClass: 'p-button-text text-slate-500 rounded-xl',
      accept: () => {
        this.missionService.unassignUser(codeMission, user.id!).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Succès', detail: `Mission ${codeMission} retirée` });
            this.loadUserMissions(user.id!);
            this.loadAvailableMissions();
          },
          error: (err) => {
            this.messageService.add({ severity: 'error', summary: 'Erreur', detail: err.error?.message || 'Erreur lors de la désassignation' });
          }
        });
      }
    });
  }

  deleteUser(user: UserResponse) {
    if (!user.username) return;

    this.confirmationService.confirm({
      message: `Êtes-vous sûr de vouloir supprimer l'utilisateur "${user.username}" ? Cette action est irréversible.`,
      header: 'Confirmation de suppression',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Supprimer',
      rejectLabel: 'Annuler',
      acceptButtonStyleClass: 'p-button-danger rounded-xl',
      rejectButtonStyleClass: 'p-button-text text-slate-500 rounded-xl',
      accept: () => {
        this.userService.deleteUser(user.username!).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Utilisateur supprimé' });
            this.loadUsers();
          },
          error: (err) => {
            this.messageService.add({ severity: 'error', summary: 'Erreur', detail: err.error?.message || 'Impossible de supprimer l\'utilisateur' });
          }
        });
      }
    });
  }
}
