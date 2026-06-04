import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeng/themes/aura';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';
import { provideApi } from './api-client/provide-api';
import { environment } from '../environments/environment';

import { routes } from './app.routes';
import { MessageService, ConfirmationService } from 'primeng/api';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideAnimationsAsync(),
    // Configuration de l'API générée
    provideApi(environment.apiUrl),
    // provideHttpClient est nécessaire pour que les services (Auth, etc.) puissent faire des requêtes API
    provideHttpClient(withInterceptors([jwtInterceptor])),
    // Configuration de PrimeNG 21
    providePrimeNG({
      theme: {
        preset: Aura, // Utilisation du thème moderne "Aura"
        options: {
          darkModeSelector: '.app-dark' // Classe utilisée pour activer le mode sombre
        }
      }
    }),
    MessageService,
    ConfirmationService
  ]
};
