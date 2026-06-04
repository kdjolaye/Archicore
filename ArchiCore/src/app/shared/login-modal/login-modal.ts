import { Component, inject, signal, Injectable, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class LoginModalService {
  visible = signal<boolean>(false);
  sessionExpired = signal<boolean>(false);

  show(expired = false) {
    this.sessionExpired.set(expired);
    this.visible.set(true);
  }

  hide() {
    this.visible.set(false);
    this.sessionExpired.set(false);
  }
}

@Component({
  selector: 'app-login-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DialogModule,
    ButtonModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './login-modal.html'
})
export class LoginModalComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private messageService = inject(MessageService);
  private router = inject(Router);
  modalService = inject(LoginModalService);

  loading = signal<boolean>(false);
  showPassword = signal<boolean>(false);

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    rememberMe: [false]
  });

  ngOnInit() {
    if (this.modalService.sessionExpired()) {
      setTimeout(() => {
        this.messageService.add({
          severity: 'warn',
          summary: 'Session expirée',
          detail: 'Votre session a expiré, veuillez vous reconnecter.',
          life: 5000
        });
      }, 300);
    }
  }

  onSubmit() {
    if (this.loginForm.invalid) return;

    this.loading.set(true);
    const { email, password } = this.loginForm.value;

    this.authService.login({ email: email!, password: password! }).subscribe({
     // login-modal.ts
next: (response) => {  // ← reçois la réponse directement
  this.loading.set(false);
  this.messageService.add({
    severity: 'success',
    summary: 'Connexion réussie',
    detail: `Bienvenue ${response.email}`,  // ← utilise response, pas le signal
    life: 2000
  });
  setTimeout(() => {
    this.modalService.hide();
    this.loginForm.reset();
    this.router.navigate(['/dossiers']);
  }, 500);
},
      error: () => {
        this.loading.set(false);
        this.messageService.add({
          severity: 'error',
          summary: 'Échec de connexion',
          detail: 'Email ou mot de passe incorrect.',
          life: 4000
        });
      }
    });
  }

  togglePassword() {
    this.showPassword.update(v => !v);
  }

  onClose() {
    this.modalService.hide();
    this.loginForm.reset();
  }
}