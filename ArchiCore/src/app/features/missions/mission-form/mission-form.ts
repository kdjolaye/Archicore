import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MissionControllerService } from '../../../api-client/api/missionController.service';
import { MissionCreateRequest } from '../../../api-client/model/missionCreateRequest';
import { MissionUpdateRequest } from '../../../api-client/model/missionUpdateRequest';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CardModule } from 'primeng/card';
import { Select } from 'primeng/select';        // ← corrigé
import { BreadcrumbComponent } from '../../../shared/breadcrumb/breadcrumb';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-mission-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ButtonModule,
    InputTextModule,
    CardModule,
    Select,                // ← corrigé
    BreadcrumbComponent
  ],
  templateUrl: './mission-form.html'
})
export class MissionFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private missionService = inject(MissionControllerService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private messageService = inject(MessageService);

  form!: FormGroup;
  isEditMode = signal(false);
  codeDossier = signal<string | null>(null);
  codeMission = signal<string | null>(null);
  loading = signal(false);

  statutOptions = [
    { label: 'Planifiée', value: 'PLANIFIEE' },
    { label: 'En Cours', value: 'EN_COURS' },
    { label: 'Terminée', value: 'TERMINEE' },
    { label: 'Suspendue', value: 'SUSPENDUE' }
  ];

  ngOnInit() {
    this.codeDossier.set(this.route.snapshot.paramMap.get('codeDossier'));
    const missionCode = this.route.snapshot.paramMap.get('codeMission');

    if (missionCode) {
      this.isEditMode.set(true);
      this.codeMission.set(missionCode);
    }

    this.initForm();

    if (this.isEditMode()) {
      this.loadMission(missionCode!);
    }
  }

  private initForm() {
    this.form = this.fb.group({
      codeMission: [{ value: '', disabled: this.isEditMode() }, [Validators.required]],
      exercice: ['', [Validators.required]],
      statut: ['PLANIFIEE']
    });
  }

  private loadMission(code: string) {
    this.loading.set(true);
    this.missionService.findByCode(code).subscribe({
      next: (mission) => {
        this.form.patchValue({
          codeMission: mission.codeMission,
          exercice: mission.exercice,
          statut: mission.statut
        });
        this.loading.set(false);
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de charger la mission'
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
      const updateReq: MissionUpdateRequest = {
        statut: data.statut,
        exercice: data.exercice
      };
      this.missionService.update(this.codeMission()!, updateReq).subscribe({
        next: () => {
          this.loading.set(false);  // ← corrigé
          this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Mission mise à jour' });
          setTimeout(() => this.router.navigate(['/dossiers', this.codeDossier(), 'missions']), 1000);
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de la mise à jour' });
          this.loading.set(false);
        }
      });
    } else {
      const createReq: MissionCreateRequest = {
        codeMission: data.codeMission,
        exercice: data.exercice,
        codeDossier: this.codeDossier()!
      };
      this.missionService.create(createReq).subscribe({
        next: () => {
          this.loading.set(false);  // ← corrigé
          this.messageService.add({ severity: 'success', summary: 'Succès', detail: 'Mission créée' });
          setTimeout(() => this.router.navigate(['/dossiers', this.codeDossier(), 'missions']), 1000);
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de la création' });
          this.loading.set(false);
        }
      });
    }
  }

  onCancel() {
    this.router.navigate(['/dossiers', this.codeDossier(), 'missions']);
  }
}