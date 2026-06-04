import { Component, inject, signal, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';

/**
 * Page de changement de mot de passe obligatoire.
 * Affichée automatiquement quand le statut est :
 *   - FORCE_PASSWORD_CHANGE : première connexion avec mot de passe temporaire
 *   - PASSWORD_EXPIRED      : expiration mensuelle (> 30 jours)
 */
@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, CardModule, ButtonModule, InputTextModule, MessageModule],
  templateUrl: './change-password.html',
  styleUrl: './change-password.css'
})
export class ChangePasswordComponent implements OnInit {
  private fb      = inject(FormBuilder);
  private router  = inject(Router);
  authService     = inject(AuthService);

  form!: FormGroup;
  loading         = signal(false);
  success         = signal(false);
  errorMsg        = signal<string | null>(null);
  showCurrent     = signal(false);
  showNew         = signal(false);
  showConfirm     = signal(false);

  /** Message contextuel selon le statut du compte */
  alertMessage = computed(() => {
    const status = this.authService.currentUser()?.status;
    if (status === 'FORCE_PASSWORD_CHANGE') {
      return {
        type: 'warning',
        title: 'Changement de mot de passe requis',
        body: 'Votre compte a été créé avec un mot de passe temporaire. Vous devez définir un nouveau mot de passe pour accéder à l\'application.'
      };
    }
    if (status === 'PASSWORD_EXPIRED') {
      return {
        type: 'danger',
        title: 'Mot de passe expiré',
        body: 'Votre mot de passe a expiré (plus de 30 jours sans modification). Pour des raisons de sécurité, veuillez le renouveler.'
      };
    }
    return null;
  });

  ngOnInit() {
    this.form = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [
        Validators.required,
        Validators.pattern(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$/)
      ]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordsMatchValidator });
  }

  private passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
    const newPwd     = group.get('newPassword')?.value;
    const confirmPwd = group.get('confirmPassword')?.value;
    return newPwd && confirmPwd && newPwd !== confirmPwd ? { passwordsMismatch: true } : null;
  }

  get passwordStrength(): { level: 'weak' | 'medium' | 'strong'; label: string; width: string } {
    const pwd = this.form?.get('newPassword')?.value || '';
    let score = 0;
    if (pwd.length >= 8)                   score++;
    if (/[A-Z]/.test(pwd))                 score++;
    if (/[0-9]/.test(pwd))                 score++;
    if (/[@#$%^&+=!]/.test(pwd))           score++;
    if (pwd.length >= 12)                  score++;
    if (score <= 2) return { level: 'weak',   label: 'Faible',  width: '33%'  };
    if (score <= 3) return { level: 'medium', label: 'Moyen',   width: '66%'  };
    return               { level: 'strong', label: 'Fort',    width: '100%' };
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMsg.set(null);

    const { currentPassword, newPassword } = this.form.value;
    this.authService.changePassword({ currentPassword, newPassword }).subscribe({
      next: () => {
        this.loading.set(false);
        this.success.set(true);
        setTimeout(() => this.router.navigate(['/dossiers']), 2000);
      },
      error: (err: any) => {
        this.loading.set(false);
        const msg = err?.error?.message || err?.error || 'Mot de passe actuel incorrect.';
        this.errorMsg.set(typeof msg === 'string' ? msg : 'Une erreur est survenue.');
      }
    });
  }

  hasError(field: string, error: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.hasError(error) && ctrl.touched);
  }

  get passwordsMismatch(): boolean {
    return !!(this.form.hasError('passwordsMismatch') && this.form.get('confirmPassword')?.touched);
  }
}
