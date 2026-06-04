import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DossierControllerService } from '../../../api-client/api/dossierController.service';
import { DossierCreateRequest } from '../../../api-client/model/dossierCreateRequest';
import { DossierUpdateRequest } from '../../../api-client/model/dossierUpdateRequest';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { Textarea } from 'primeng/textarea';        // ← corrigé
import { CardModule } from 'primeng/card';
import { Select } from 'primeng/select';
import { BreadcrumbComponent } from '../../../shared/breadcrumb/breadcrumb';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-dossier-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    Textarea,           // ← corrigé
    CardModule,
    Select,             // ← virgule ajoutée
    BreadcrumbComponent
  ],
  templateUrl: './dossier-form.html'
})
export class DossierFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dossierService = inject(DossierControllerService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private messageService = inject(MessageService);

  form!: FormGroup;
  isEditMode = signal(false);
  codeDossier = signal<string | null>(null);
  loading = signal(false);

  typeOptions = [
    { label: 'SARL', value: 'SARL' },
    { label: 'SA', value: 'SA' },
    { label: 'SAS', value: 'SAS' },
    { label: 'EIRL', value: 'EIRL' },
    { label: 'Particulier', value: 'PARTICULIER' }
  ];

  ngOnInit() {
    const code = this.route.snapshot.paramMap.get('codeDossier');
    if (code) {
      this.isEditMode.set(true);
      this.codeDossier.set(code);
    }
    this.initForm();
    if (code) {
      this.loadDossier(code);
    }
  }

  private initForm() {
    this.form = this.fb.group({
      codeDossier:    [{ value: '', disabled: this.isEditMode() }, [Validators.required]],
      raisonSociale:  ['', [Validators.required]],
      typeEntreprise: [''],
      adresse:        [''],
      phone:          [''],
      email:          ['', [Validators.email]],
      bp:             ['']
    });
  }

  private loadDossier(code: string) {
    this.loading.set(true);
    this.dossierService.getByCode(code).subscribe({
      next: (dossier) => {
        this.form.patchValue(dossier);
        this.loading.set(false);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger le dossier'
        });
        this.loading.set(false);
      }
    });
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.loading.set(true);
    const data = this.form.getRawValue();

    if (this.isEditMode()) {
      const updateReq: DossierUpdateRequest = { ...data };
      this.dossierService.update1(this.codeDossier()!, updateReq).subscribe({
        next: () => {
          this.loading.set(false);   // ← corrigé
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Dossier mis à jour'
          });
          setTimeout(() => this.router.navigate(['/dossiers']), 1000);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Échec de la mise à jour'
          });
          this.loading.set(false);
        }
      });
    } else {
      const createReq: DossierCreateRequest = data;
      this.dossierService.create1(createReq).subscribe({
        next: () => {
          this.loading.set(false);   // ← corrigé
          this.messageService.add({
            severity: 'success',
            summary: 'Succès',
            detail: 'Dossier créé'
          });
          setTimeout(() => this.router.navigate(['/dossiers']), 1000);
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: 'Erreur',
            detail: 'Échec de la création'
          });
          this.loading.set(false);
        }
      });
    }
  }

  onCancel() {
    this.router.navigate(['/dossiers']);
  }
}