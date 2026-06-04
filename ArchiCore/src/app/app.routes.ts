import { Routes } from '@angular/router';
import { DossierListComponent } from './features/dossiers/dossier-list/dossier-list';
import { DossierDetailComponent } from './features/dossiers/dossier-detail/dossier-detail';
import { DossierFormComponent } from './features/dossiers/dossier-form/dossier-form';
import { MissionListComponent } from './features/missions/mission-list/mission-list';
import { MissionDetailComponent } from './features/missions/mission-detail/mission-detail';
import { MissionFormComponent } from './features/missions/mission-form/mission-form';
import { DocumentListComponent } from './features/documents/document-list/document-list';
import { authGuard } from './core/guards/auth-guard';
import { publicGuard } from './core/guards/public-guard';

import { ProfileComponent } from './features/profile/profile';
import { ChangePasswordComponent } from './features/change-password/change-password';
import { UserListComponent } from './features/users/user-list/user-list';
import { RolesPermissionsComponent } from './features/roles-permissions/roles-permissions';
import { AuditListComponent } from './features/audit/audit-list/audit-list';

export const routes: Routes = [
  // Redirection racine
  { path: '', redirectTo: 'dossiers', pathMatch: 'full' },

  // ─── Page publique ───────────────────────────────────────────────────────
  // Dossiers list : accessible sans connexion (lecture publique)
  { path: 'dossiers', component: DossierListComponent },

  // ─── Pages protégées ─────────────────────────────────────────────────────
  // Administration
  { path: 'users',                          canActivate: [authGuard], component: UserListComponent },
  { path: 'roles-permissions',              canActivate: [authGuard], component: RolesPermissionsComponent },
  { path: 'audit',                          canActivate: [authGuard], component: AuditListComponent },

  // Auto-service
  { path: 'change-password',                canActivate: [authGuard], component: ChangePasswordComponent },
  { path: 'profile',                        canActivate: [authGuard], component: ProfileComponent },

  // Dossiers
  { path: 'dossiers/new',                   canActivate: [authGuard], component: DossierFormComponent },
  { path: 'dossiers/:codeDossier/detail',   canActivate: [authGuard], component: DossierDetailComponent },
  { path: 'dossiers/:codeDossier/edit',     canActivate: [authGuard], component: DossierFormComponent },

  // Missions
  { path: 'dossiers/:codeDossier/missions',                                         canActivate: [authGuard], component: MissionListComponent },
  { path: 'dossiers/:codeDossier/missions/new',                                     canActivate: [authGuard], component: MissionFormComponent },
  { path: 'dossiers/:codeDossier/missions/:codeMission/detail',                     canActivate: [authGuard], component: MissionDetailComponent },
  { path: 'dossiers/:codeDossier/missions/:codeMission/edit',                       canActivate: [authGuard], component: MissionFormComponent },

  // Documents
  { path: 'dossiers/:codeDossier/missions/:codeMission/documents',                  canActivate: [authGuard], component: DocumentListComponent },

  // Redirection par défaut
  { path: '**', redirectTo: 'dossiers' }
];