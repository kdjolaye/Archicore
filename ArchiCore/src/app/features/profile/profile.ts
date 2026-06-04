import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { UserControllerService } from '../../api-client/api/userController.service';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageService } from 'primeng/api';

/**
 * Page de profil utilisateur.
 * Deux sections indépendantes :
 *   1. Informations personnelles (nom, prénom, téléphone)
 *   2. Sécurité (changement de mot de passe optionnel)
 */
@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CardModule, ButtonModule, InputTextModule],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class ProfileComponent implements OnInit {
  private fb     = inject(FormBuilder);
  private router = inject(Router);
  authService    = inject(AuthService);
  private userService = inject(UserControllerService);
  private messageService = inject(MessageService);

  // ── Section Profil ──────────────────────────────────────
  profileForm!: FormGroup;
  profileLoading  = signal(false);

  // ── Section Mot de passe ─────────────────────────────────
  passwordForm!: FormGroup;
  pwdLoading      = signal(false);

  showCurrent  = signal(false);
  showNew      = signal(false);
  showConfirm  = signal(false);

  // Onglet actif
  activeTab = signal<'info' | 'security'>('info');

  ngOnInit() {
    const user = this.authService.currentUser();

    this.profileForm = this.fb.group({
      name:    ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      surname: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      phone:   ['', [Validators.pattern(/^\+?[0-9]{10,15}$/)]]
    });

    this.loadProfile();

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword:     ['', [
        Validators.required,
        Validators.pattern(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$/)
      ]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordsMatchValidator });
  }

  loadProfile() {
    this.userService.getMyProfile().subscribe({
      next: (user) => {
        this.profileForm.patchValue({
          name: user.name || '',
          surname: user.surname || '',
          phone: user.phone || ''
        });
      },
      error: (err) => {
        console.error('Erreur chargement profil', err);
      }
    });
  }

  private passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
    const newPwd     = group.get('newPassword')?.value;
    const confirmPwd = group.get('confirmPassword')?.value;
    return newPwd && confirmPwd && newPwd !== confirmPwd ? { passwordsMismatch: true } : null;
  }

  get passwordStrength(): { level: 'weak' | 'medium' | 'strong'; label: string; width: string } {
    const pwd = this.passwordForm?.get('newPassword')?.value || '';
    let score = 0;
    if (pwd.length >= 8)         score++;
    if (/[A-Z]/.test(pwd))       score++;
    if (/[0-9]/.test(pwd))       score++;
    if (/[@#$%^&+=!]/.test(pwd)) score++;
    if (pwd.length >= 12)        score++;
    if (score <= 2) return { level: 'weak',   label: 'Faible', width: '33%'  };
    if (score <= 3) return { level: 'medium', label: 'Moyen',  width: '66%'  };
    return               { level: 'strong', label: 'Fort',   width: '100%' };
  }

  onProfileSubmit() {
    if (this.profileForm.invalid) { this.profileForm.markAllAsTouched(); return; }
    this.profileLoading.set(true);

    this.authService.updateProfile(this.profileForm.value).subscribe({
      next: () => {
        this.profileLoading.set(false);
        this.messageService.add({
          severity: 'success',
          summary: 'Profil mis à jour',
          detail: 'Vos informations personnelles ont été modifiées avec succès.'
        });
      },
      error: (err: any) => {
        this.profileLoading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: err?.error?.message || 'Erreur lors de la mise à jour du profil.'
        });
      }
    });
  }

  onPasswordSubmit() {
    if (this.passwordForm.invalid) { this.passwordForm.markAllAsTouched(); return; }
    this.pwdLoading.set(true);

    const { currentPassword, newPassword } = this.passwordForm.value;
    this.authService.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.pwdLoading.set(false);
        this.passwordForm.reset();
        this.messageService.add({
          severity: 'success',
          summary: 'Mot de passe modifié',
          detail: 'Votre mot de passe a été mis à jour avec succès.'
        });
      },
      error: (err: any) => {
        this.pwdLoading.set(false);
        const msg = err?.error?.message || err?.error || 'Mot de passe actuel incorrect.';
        this.messageService.add({
          severity: 'error',
          summary: 'Erreur',
          detail: typeof msg === 'string' ? msg : 'Une erreur est survenue.'
        });
      }
    });
  }

  hasProfileError(field: string, error: string): boolean {
    const ctrl = this.profileForm.get(field);
    return !!(ctrl?.hasError(error) && ctrl.touched);
  }

  hasPwdError(field: string, error: string): boolean {
    const ctrl = this.passwordForm.get(field);
    return !!(ctrl?.hasError(error) && ctrl.touched);
  }

  get passwordsMismatch(): boolean {
    return !!(this.passwordForm.hasError('passwordsMismatch') && this.passwordForm.get('confirmPassword')?.touched);
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

  getStatusLabel(status: string | undefined): { label: string; css: string } {
    const map: Record<string, { label: string; css: string }> = {
      ACTIVE:               { label: 'Actif',                  css: 'status-active'   },
      FORCE_PASSWORD_CHANGE:{ label: 'Mot de passe temporaire',css: 'status-warning'  },
      PASSWORD_EXPIRED:     { label: 'Mot de passe expiré',    css: 'status-danger'   },
      SUSPENDED:            { label: 'Suspendu',               css: 'status-suspended'},
    };
    return status ? (map[status] || { label: status, css: '' }) : { label: '—', css: '' };
  }

  goBack() { this.router.navigate(['/dossiers']); }
}
