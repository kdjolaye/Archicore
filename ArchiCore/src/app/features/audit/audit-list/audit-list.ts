import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

// Services
import { AuditControllerService } from '../../../api-client/api/auditController.service';
import { UserControllerService } from '../../../api-client/api/userController.service';
import { AuthService } from '../../../core/services/auth.service';

// Models
import { AuditLog } from '../../../api-client/model/auditLog';

// PrimeNG
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { SelectModule } from 'primeng/select';
import { MessageService } from 'primeng/api';
import { TooltipModule } from 'primeng/tooltip';

@Component({
  selector: 'app-audit-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    TableModule,
    ButtonModule,
    CardModule,
    SelectModule,
    TooltipModule
  ],
  templateUrl: './audit-list.html',
  styleUrl: './audit-list.css'
})
export class AuditListComponent implements OnInit {
  private auditService = inject(AuditControllerService);
  private userService = inject(UserControllerService);
  private messageService = inject(MessageService);
  private router = inject(Router);
  authService = inject(AuthService);

  // États du dashboard
  activeTab = signal<'tous' | 'utilisateur' | 'ressource' | 'periode' | 'action'>('tous');
  logs = signal<AuditLog[]>([]);
  loading = signal(false);

  // Options et filtres
  users = signal<{ label: string; value: string }[]>([]);
  
  // Paramètres des filtres
  selectedUsername = signal<string>('');
  selectedResourceType = signal<string>('DOSSIER');
  resourceId = signal<string>('');
  startDate = signal<string>('');
  endDate = signal<string>('');
  selectedAction = signal<any>(null);

  // Listes statiques d'options
  resourceTypeOptions = [
    { label: 'Dossier', value: 'DOSSIER' },
    { label: 'Mission', value: 'MISSION' },
    { label: 'Document', value: 'DOCUMENT' },
    { label: 'Équipe', value: 'EQUIPE' }
  ];

  actionOptions = [
    { label: 'Création Dossier', value: 'CREER_DOSSIER' },
    { label: 'Modification Dossier', value: 'UPDATE_DOSSIER' },
    { label: 'Suppression Dossier', value: 'SUPPRIMER_DOSSIER' },
    { label: 'Création Mission', value: 'CREER_MISSION' },
    { label: 'Modification Mission', value: 'UPDATE_MISSION' },
    { label: 'Suppression Mission', value: 'SUPPRIMER_MISSION' },
    { label: 'Constitution Équipe', value: 'CONSTITUER_EQUIPE' },
    { label: 'Upload Document', value: 'UPLOAD_DOCUMENT' },
    { label: 'Consultation Document', value: 'CONSULTER_DOCUMENT' },
    { label: 'Téléchargement Document', value: 'TELECHARGER_DOCUMENT' },
    { label: 'Suppression Document', value: 'SUPPRIMER_DOCUMENT' }
  ];

  ngOnInit() {
    // Sécurité : redirection si non admin
    if (!this.authService.hasRole(['SUPER_ADMIN', 'ADMIN_PRINCIPAL'])) {
      this.router.navigate(['/dossiers']);
      return;
    }

    this.loadUsers();
    this.fetchLogs();
  }

  loadUsers() {
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.users.set(data.map(u => ({
          label: u.username || '',
          value: u.username || ''
        })));
      },
      error: (err) => {
        console.error('Erreur chargement utilisateurs', err);
      }
    });
  }

  // Sélection de l'onglet
  switchTab(tab: 'tous' | 'utilisateur' | 'ressource' | 'periode' | 'action') {
    this.activeTab.set(tab);
    this.logs.set([]); // Vider le tableau entre deux recherches
    
    // Si l'onglet est "Tous", on lance la recherche automatiquement
    if (tab === 'tous') {
      this.fetchLogs();
    }
  }

  // Déclencher la recherche
  fetchLogs() {
    this.loading.set(true);
    const tab = this.activeTab();

    if (tab === 'tous') {
      this.auditService.getTous().subscribe({
        next: (data) => this.handleSuccess(data),
        error: (err) => this.handleError(err)
      });
    } else if (tab === 'utilisateur') {
      const username = this.selectedUsername();
      if (!username) {
        this.loading.set(false);
        this.messageService.add({ severity: 'warn', summary: 'Attention', detail: 'Veuillez sélectionner un utilisateur.' });
        return;
      }
      this.auditService.getByUtilisateur(username).subscribe({
        next: (data) => this.handleSuccess(data),
        error: (err) => this.handleError(err)
      });
    } else if (tab === 'ressource') {
      const type = this.selectedResourceType();
      const id = this.resourceId();
      if (!type || !id) {
        this.loading.set(false);
        this.messageService.add({ severity: 'warn', summary: 'Attention', detail: 'Veuillez renseigner le type et l\'identifiant de la ressource.' });
        return;
      }
      this.auditService.getByRessource(type, id).subscribe({
        next: (data) => this.handleSuccess(data),
        error: (err) => this.handleError(err)
      });
    } else if (tab === 'periode') {
      const debut = this.startDate();
      const fin = this.endDate();
      if (!debut || !fin) {
        this.loading.set(false);
        this.messageService.add({ severity: 'warn', summary: 'Attention', detail: 'Veuillez renseigner les dates de début et de fin.' });
        return;
      }
      this.auditService.getByPeriode(debut, fin).subscribe({
        next: (data) => this.handleSuccess(data),
        error: (err) => this.handleError(err)
      });
    } else if (tab === 'action') {
      const action = this.selectedAction();
      if (!action) {
        this.loading.set(false);
        this.messageService.add({ severity: 'warn', summary: 'Attention', detail: 'Veuillez sélectionner une action.' });
        return;
      }
      this.auditService.getByAction(action).subscribe({
        next: (data) => this.handleSuccess(data),
        error: (err) => this.handleError(err)
      });
    }
  }

  private handleSuccess(data: AuditLog[]) {
    // Trier du plus récent au plus ancien par sécurité
    const sorted = [...(data || [])].sort((a, b) => {
      const dateA = a.performedAt ? new Date(a.performedAt).getTime() : 0;
      const dateB = b.performedAt ? new Date(b.performedAt).getTime() : 0;
      return dateB - dateA;
    });
    this.logs.set(sorted);
    this.loading.set(false);
  }

  private handleError(err: any) {
    this.loading.set(false);
    this.messageService.add({
      severity: 'error',
      summary: 'Erreur',
      detail: err.error?.message || 'Erreur lors du chargement des logs d\'audit.'
    });
  }

  // Traduction visuelle des actions
  getActionLabel(action: string | undefined): string {
    if (!action) return '—';
    const found = this.actionOptions.find(o => o.value === action);
    return found ? found.label : action;
  }

  getActionBadgeClass(action: string | undefined): string {
    if (!action) return 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300 border-slate-200 dark:border-slate-700';

    if (action.startsWith('CREER') || action === 'UPLOAD_DOCUMENT') {
      return 'bg-emerald-50 text-emerald-700 border-emerald-100 dark:bg-emerald-500/10 dark:text-emerald-400 dark:border-emerald-500/20';
    }
    if (action.startsWith('UPDATE') || action === 'CONSTITUER_EQUIPE') {
      return 'bg-amber-50 text-amber-700 border-amber-100 dark:bg-amber-500/10 dark:text-amber-400 dark:border-amber-500/20';
    }
    if (action.startsWith('SUPPRIMER')) {
      return 'bg-rose-50 text-rose-700 border-rose-100 dark:bg-rose-500/10 dark:text-rose-400 dark:border-rose-500/20';
    }
    if (action.startsWith('CONSULTER') || action === 'TELECHARGER_DOCUMENT') {
      return 'bg-blue-50 text-blue-700 border-blue-100 dark:bg-blue-500/10 dark:text-blue-400 dark:border-blue-500/20';
    }

    return 'bg-slate-100 text-slate-700 border-slate-200 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700';
  }

  goBack() {
    this.router.navigate(['/dossiers']);
  }
}
