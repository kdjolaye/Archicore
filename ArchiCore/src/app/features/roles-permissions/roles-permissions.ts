import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

// Services API
import { RoleControllerService } from '../../api-client/api/roleController.service';
import { PermissionControllerService } from '../../api-client/api/permissionController.service';
import { AuthService } from '../../core/services/auth.service';

// Model interfaces
import { Role } from '../../api-client/model/role';
import { Permission } from '../../api-client/model/permission';

// PrimeNG
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { CheckboxModule } from 'primeng/checkbox';
import { MessageService } from 'primeng/api';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-roles-permissions',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    TableModule,
    ButtonModule,
    CardModule,
    DialogModule,
    InputTextModule,
    CheckboxModule,
    TooltipModule
  ],
  templateUrl: './roles-permissions.html',
  styleUrl: './roles-permissions.css'
})
export class RolesPermissionsComponent implements OnInit {
  private roleService = inject(RoleControllerService);
  private permissionService = inject(PermissionControllerService);
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);
  private router = inject(Router);
  authService = inject(AuthService);

  // Signaux d'état
  roles = signal<Role[]>([]);
  permissions = signal<Permission[]>([]);
  loadingRoles = signal(true);
  loadingPermissions = signal(true);
  activeTab = signal<'roles' | 'permissions'>('roles');

  // Dialogues
  showCreateRoleDialog = signal(false);
  showEditRolePermissionsDialog = signal(false);
  showCreatePermissionDialog = signal(false);

  // Formulaires
  createRoleForm!: FormGroup;
  createPermissionForm!: FormGroup;
  editRolePermissionsForm!: FormGroup;

  isSubmittingRole = signal(false);
  isSubmittingPermission = signal(false);
  isUpdatingPermissions = signal(false);

  // Rôle en cours d'édition
  selectedRoleForPermissions = signal<Role | null>(null);

  // Liste des permissions sous forme d'options pour sélection
  permissionOptions = computed(() => {
    return this.permissions().map(p => ({
      label: p.name || '',
      value: p.name || ''
    }));
  });

  ngOnInit() {
    // Restriction d'accès rigoureuse
    if (!this.authService.hasRole(['SUPER_ADMIN', 'ADMIN_PRINCIPAL'])) {
      this.router.navigate(['/dossiers']);
      return;
    }

    this.initForms();
    this.loadRoles();
    this.loadPermissions();
  }

  initForms() {
    this.createRoleForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      permissions: [[]] // Tableau de noms de permissions sélectionnées
    });

    this.createPermissionForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]]
    });

    this.editRolePermissionsForm = this.fb.group({
      permissions: [[]] // Tableau de noms de permissions sélectionnées
    });
  }

  loadRoles() {
    this.loadingRoles.set(true);
    this.roleService.getAllRoles().subscribe({
      next: (data) => {
        this.roles.set(data);
        this.loadingRoles.set(false);
      },
      error: () => {
        this.loadingRoles.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger la liste des rôles.'
        });
      }
    });
  }

  loadPermissions() {
    this.loadingPermissions.set(true);
    this.permissionService.getAllPermissions().subscribe({
      next: (data) => {
        this.permissions.set(data);
        this.loadingPermissions.set(false);
      },
      error: () => {
        this.loadingPermissions.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger la liste des permissions.'
        });
      }
    });
  }

  // ─── CRUD RÔLES ─────────────────────────────────────────────────────────

  openCreateRoleDialog() {
    this.createRoleForm.reset({
      name: '',
      permissions: []
    });
    this.showCreateRoleDialog.set(true);
  }

  onCreateRoleSubmit() {
    if (this.createRoleForm.invalid) return;

    this.isSubmittingRole.set(true);
    const { name, permissions } = this.createRoleForm.value;

    // Conversion en Set casté pour le client API
    const permissionNamesSet = Array.from(permissions || []) as unknown as Set<string>;

    this.roleService.createRole({ name, permissionNames: permissionNamesSet }).subscribe({
      next: () => {
        this.isSubmittingRole.set(false);
        this.showCreateRoleDialog.set(false);
        this.loadRoles();
        this.messageService.add({
          severity: 'success',
          summary: 'Rôle créé',
          detail: `Le rôle "${name}" a été créé avec succès.`
        });
      },
      error: (err) => {
        this.isSubmittingRole.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: err.error?.message || 'Erreur lors de la création du rôle.'
        });
      }
    });
  }

  // ─── MODIFICATION DES PERMISSIONS D'UN RÔLE ─────────────────────────────

  openEditPermissionsDialog(role: Role) {
    this.selectedRoleForPermissions.set(role);
    
    // Extraire les noms des permissions associées au rôle
    const assignedPermissionNames = this.getPermissionsArray(role.permissions).map(p => p.name || '');

    this.editRolePermissionsForm.patchValue({
      permissions: assignedPermissionNames
    });
    this.showEditRolePermissionsDialog.set(true);
  }

  onEditPermissionsSubmit() {
    const role = this.selectedRoleForPermissions();
    if (!role || !role.id) return;

    this.isUpdatingPermissions.set(true);
    const { permissions } = this.editRolePermissionsForm.value;

    // Conversion en Set casté pour le client API
    const permissionNamesSet = Array.from(permissions || []) as unknown as Set<string>;

    this.roleService.updatePermissions(role.id, permissionNamesSet).subscribe({
      next: () => {
        this.isUpdatingPermissions.set(false);
        this.showEditRolePermissionsDialog.set(false);
        this.loadRoles();
        this.messageService.add({
          severity: 'success',
          summary: 'Permissions mises à jour',
          detail: `Les droits du rôle "${role.name}" ont été mis à jour.`
        });
      },
      error: (err) => {
        this.isUpdatingPermissions.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: err.error?.message || 'Erreur lors de la mise à jour des droits.'
        });
      }
    });
  }

  // Helper pour gérer la sélection de permissions sous forme de checkboxes réactives
  onPermissionCheckboxChange(event: any, permissionName: string, controlName: 'permissions' | 'editPermissions') {
    const form = controlName === 'permissions' ? this.createRoleForm : this.editRolePermissionsForm;
    const selected = form.get('permissions')?.value as string[] || [];
    
    if (event.checked) {
      if (!selected.includes(permissionName)) {
        form.patchValue({ permissions: [...selected, permissionName] });
      }
    } else {
      form.patchValue({ permissions: selected.filter(p => p !== permissionName) });
    }
  }

  isPermissionChecked(permissionName: string, controlName: 'permissions' | 'editPermissions'): boolean {
    const form = controlName === 'permissions' ? this.createRoleForm : this.editRolePermissionsForm;
    const selected = form.get('permissions')?.value as string[] || [];
    return selected.includes(permissionName);
  }

  // ─── CRUD PERMISSIONS ───────────────────────────────────────────────────

  openCreatePermissionDialog() {
    this.createPermissionForm.reset({ name: '' });
    this.showCreatePermissionDialog.set(true);
  }

  onCreatePermissionSubmit() {
    if (this.createPermissionForm.invalid) return;

    this.isSubmittingPermission.set(true);
    const { name } = this.createPermissionForm.value;

    this.permissionService.createPermission({ name }).subscribe({
      next: () => {
        this.isSubmittingPermission.set(false);
        this.showCreatePermissionDialog.set(false);
        this.loadPermissions();
        this.messageService.add({
          severity: 'success',
          summary: 'Permission créée',
          detail: `La permission "${name}" a été enregistrée.`
        });
      },
      error: (err) => {
        this.isSubmittingPermission.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: err.error?.message || 'Erreur lors de la création de la permission.'
        });
      }
    });
  }

  // Helpers UI
  getPermissionsArray(permissions: any): Permission[] {
    if (!permissions) return [];
    if (Array.isArray(permissions)) return permissions;
    if (permissions instanceof Set) return Array.from(permissions);
    return [];
  }

  getRoleLabel(roleName: string | undefined): string {
    const labels: Record<string, string> = {
      SUPER_ADMIN: 'Super Administrateur',
      ADMIN_PRINCIPAL: 'Administrateur Principal',
      ADMIN: 'Administrateur',
      CONSULTANT: 'Consultant'
    };
    return roleName ? (labels[roleName] || roleName) : '—';
  }

  goBack() {
    this.router.navigate(['/dossiers']);
  }
}
