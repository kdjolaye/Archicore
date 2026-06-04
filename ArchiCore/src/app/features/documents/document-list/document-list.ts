import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DocumentControllerService } from '../../../api-client/api/documentController.service';
import { MissionControllerService } from '../../../api-client/api/missionController.service';
import { DocumentResponse } from '../../../api-client/model/documentResponse';
import { AuthService } from '../../../core/services/auth.service';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { TooltipModule } from 'primeng/tooltip';
import { BreadcrumbComponent } from '../../../shared/breadcrumb/breadcrumb';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { MessageService, ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    ButtonModule,
    CardModule,
    TooltipModule,
    BreadcrumbComponent,
    RouterLink
  ],
  templateUrl: './document-list.html'
})
export class DocumentListComponent implements OnInit {
  private documentService = inject(DocumentControllerService);
  private missionService = inject(MissionControllerService);
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  authService = inject(AuthService);

  codeDossier = signal<string>('');
  codeMission = signal<string>('');
  documents = signal<DocumentResponse[]>([]);
  loading = signal<boolean>(true);
  uploading = signal<boolean>(false);
  isAssigned = signal<boolean>(false);

  // Peut uploader : SUPER_ADMIN et ADMIN_PRINCIPAL toujours,
  // ADMIN uniquement si assigné, CONSULTANT jamais
  canUpload = computed(() => {
    if (this.authService.hasRole(['SUPER_ADMIN', 'ADMIN_PRINCIPAL'])) return true;
    if (this.authService.hasRole(['ADMIN'])) return this.isAssigned();
    return false; // CONSULTANT et autres
  });

  // Peut supprimer : même règle que upload
  canDelete = computed(() => {
    if (this.authService.hasRole(['SUPER_ADMIN', 'ADMIN_PRINCIPAL'])) return true;
    if (this.authService.hasRole(['ADMIN'])) return this.isAssigned();
    return false;
  });

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.codeDossier.set(params['codeDossier']);
      this.codeMission.set(params['codeMission']);
      this.loadDocuments();
      this.checkAssignment();
    });
  }

  checkAssignment() {
    // SUPER_ADMIN et ADMIN_PRINCIPAL : accès total, pas besoin de vérifier
    if (this.authService.hasRole(['SUPER_ADMIN', 'ADMIN_PRINCIPAL'])) {
      this.isAssigned.set(true);
      return;
    }

    // ADMIN : vérifier l'assignation via l'endpoint dédié
    if (this.authService.hasRole(['ADMIN'])) {
      this.missionService.isAssignedToMe(this.codeMission()).subscribe({
        next: (assigned) => this.isAssigned.set(assigned),
        error: (err) => {
          console.error('Erreur vérification assignation document', err);
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

  loadDocuments() {
    this.loading.set(true);
    this.documentService.list(this.codeDossier(), this.codeMission()).subscribe({
      next: (data) => {
        this.documents.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erreur documents', err);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger la liste des documents'
        });
        this.loading.set(false);
      }
    });
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) this.uploadFile(file);
  }

  uploadFile(file: File) {
    this.uploading.set(true);
    const formData = new FormData();
    formData.append('file', file);
    formData.append('codeDossier', this.codeDossier());
    formData.append('codeMission', this.codeMission());

    this.http.post<DocumentResponse>(`${environment.apiUrl}/api/v1/documents/upload`, formData, {
      withCredentials: true
    }).subscribe({
      next: () => {
        this.uploading.set(false);
        this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Document importé avec succès' });
        this.loadDocuments();
      },
      error: (err) => {
        this.uploading.set(false);
        this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de l\'import du document' });
        console.error('Erreur upload', err);
      }
    });
  }

  download(id: string | undefined, filename: string | undefined) {
    if (!id) {
        console.warn('id est undefined');
        return;
    }
    
    this.messageService.add({ severity: 'info', summary: 'Téléchargement', detail: 'Préparation du fichier...' });
    this.documentService.download(id).subscribe({
        next: (blob) => {
            console.log('blob reçu', blob);
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename || 'document';
            document.body.appendChild(a); // ← nécessaire sur certains navigateurs
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
            this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Téléchargement démarré' });
        },
        error: (err) => {
            console.error('Erreur téléchargement', err);
            this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de télécharger le fichier' });
        }
    });
  }

  deleteDocument(id: string | undefined) {
    if (!id) return;
    this.confirmationService.confirm({
      message: 'Voulez-vous vraiment supprimer ce document ? Cette action est irréversible.',
      header: 'Confirmation de suppression',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Supprimer',
      rejectLabel: 'Annuler',
      acceptButtonStyleClass: 'p-button-danger rounded-xl',
      rejectButtonStyleClass: 'p-button-text text-slate-500 rounded-xl',
      accept: () => {
        this.documentService.delete2(id).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Supprimé', detail: 'Document supprimé' });
            this.loadDocuments();
          },
          error: (err) => {
            this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de la suppression du document' });
            console.error('Erreur suppression', err);
          }
        });
      }
    });
  }

  formatSize(bytes: number | undefined): string {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }
  formatType(mimeType: string | undefined): string {
  if (!mimeType) return 'Inconnu';

  const types: Record<string, string> = {
    'application/pdf': 'PDF',
    'application/msword': 'Word',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'Word',
    'application/vnd.ms-excel': 'Excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'Excel',
    'application/vnd.ms-powerpoint': 'PowerPoint',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'PowerPoint',
    'image/jpeg': 'Image JPEG',
    'image/png': 'Image PNG',
    'image/gif': 'Image GIF',
    'image/webp': 'Image WebP',
    'text/plain': 'Texte',
    'text/csv': 'CSV',
    'application/zip': 'ZIP',
    'application/x-rar-compressed': 'RAR',
  };

  return types[mimeType] ?? mimeType.split('/')[1]?.toUpperCase() ?? 'Fichier';
}
}